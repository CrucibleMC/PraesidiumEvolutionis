package io.github.cruciblemc.praesidium_evolutionis.scheduler;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import io.github.cruciblemc.praesidium_evolutionis.PraesidiumEvolutionis;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.ForgeScheduler;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.ForgeTask;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.ForgeWorker;
import org.apiguardian.api.API;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@API(status = API.Status.INTERNAL)
public class ForgeSchedulerImpl implements ForgeScheduler {

    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    private final AtomicInteger ids = new AtomicInteger(1);
    private final PriorityQueue<ForgeTaskImpl> pending = new PriorityQueue<>(10,
            (o1, o2) -> (int) (o1.getNextRun() - o2.getNextRun()));

    private final List<ForgeTaskImpl> temp = new ArrayList<>();

    private final ConcurrentHashMap<Integer, ForgeTaskImpl> runners = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newCachedThreadPool(new com.google.common.util.concurrent.ThreadFactoryBuilder().setNameFormat("ReMixed Scheduler Thread - %1$d").build());
    private final Side mySide;
    private final AtomicReference<ForgeTaskImpl> tail;
    private volatile ForgeTaskImpl head;
    private volatile int currentTick = -1;
    private ForgeAsyncDebugger debugHead = new ForgeAsyncDebugger(-1, null) {
        @Override
        void debugTo(StringBuilder string) {
        }
    };
    private ForgeAsyncDebugger debugTail = debugHead;
    private int counter = 0;

    public ForgeSchedulerImpl(Side side) {
        this.mySide = side;
        this.head = new ForgeTaskImpl(side);
        this.tail = new AtomicReference<>(head);
    }

    private static void validate(final Object task) {
        Objects.requireNonNull(task, "Task cannot be null");
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && mySide.isServer()) {
            ++counter;
            mainThreadHeartbeat(counter);
        }
    }

    public void resetState() {
        cancelAllTasks();
        mainThreadHeartbeat(counter);
        counter = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && mySide.isClient()) {
            ++counter;
            mainThreadHeartbeat(counter);
        }
    }


    public int scheduleSyncDelayedTask(ModContainer mod, final Runnable task) {
        return this.scheduleSyncDelayedTask(mod, task, 0L);
    }

    public ForgeTask runTask(ModContainer mod, Runnable runnable) {
        return runTaskLater(mod, runnable, 0L);
    }

    @Deprecated
    public int scheduleAsyncDelayedTask(ModContainer mod, final Runnable task) {
        return this.scheduleAsyncDelayedTask(mod, task, 0L);
    }

    public ForgeTask runTaskAsynchronously(ModContainer mod, Runnable runnable) {
        return runTaskLaterAsynchronously(mod, runnable, 0L);
    }

    public int scheduleSyncDelayedTask(ModContainer mod, final Runnable task, final long delay) {
        return this.scheduleSyncRepeatingTask(mod, task, delay, -1L);
    }

    public ForgeTask runTaskLater(ModContainer mod, Runnable runnable, long delay) {
        return runTaskTimer(mod, runnable, delay, -1L);
    }

    @Deprecated
    public int scheduleAsyncDelayedTask(ModContainer mod, final Runnable task, final long delay) {
        return this.scheduleAsyncRepeatingTask(mod, task, delay, -1L);
    }

    public ForgeTask runTaskLaterAsynchronously(ModContainer mod, Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(mod, runnable, delay, -1L);
    }

    public int scheduleSyncRepeatingTask(ModContainer mod, final Runnable runnable, long delay, long period) {
        return runTaskTimer(mod, runnable, delay, period).getTaskId();
    }

    public ForgeTask runTaskTimer(ModContainer mod, Runnable runnable, long delay, long period) {
        validate(runnable);
        if (delay < 0L) {
            delay = 0;
        }
        if (period == 0L) {
            period = 1L;
        } else if (period < -1L) {
            period = -1L;
        }
        return handle(new ForgeTaskImpl(runnable, mod, nextId(), period, mySide), delay);
    }

    @Deprecated
    public int scheduleAsyncRepeatingTask(ModContainer mod, final Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(mod, runnable, delay, period).getTaskId();
    }

    public ForgeTask runTaskTimerAsynchronously(ModContainer mod, Runnable runnable, long delay, long period) {
        validate(runnable);
        if (delay < 0L) {
            delay = 0;
        }
        if (period == 0L) {
            period = 1L;
        } else if (period < -1L) {
            period = -1L;
        }
        return handle(new ForgeAsyncTaskImpl(runners, mod, runnable, nextId(), period, mySide), delay);
    }

    public <T> Future<T> callSyncMethod(ModContainer mod, final Callable<T> task) {
        validate(task);
        final ForgeFutureImpl<T> future = new ForgeFutureImpl<>(task, mod, nextId(), mySide);
        handle(future, 0L);
        return future;
    }

    public void cancelTask(final int taskId) {
        if (taskId <= 0) {
            return;
        }
        ForgeTaskImpl task = runners.get(taskId);
        if (task != null) {
            task.cancel0();
        }
        task = new ForgeTaskImpl(
                new Runnable() {
                    public void run() {
                        if (!check(temp)) {
                            check(pending);
                        }
                    }

                    private boolean check(final Iterable<ForgeTaskImpl> collection) {
                        final Iterator<ForgeTaskImpl> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final ForgeTaskImpl task = tasks.next();
                            if (task.getTaskId() == taskId) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(taskId);
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                }, mySide);
        handle(task, 0L);
        for (ForgeTaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() == taskId) {
                taskPending.cancel0();
            }
        }
    }

    public void cancelTasks(final ModContainer mod) {
        Objects.requireNonNull(mod, "Cannot cancel tasks of null mod");
        final ForgeTaskImpl task = new ForgeTaskImpl(
                new Runnable() {
                    public void run() {
                        check(ForgeSchedulerImpl.this.pending);
                        check(ForgeSchedulerImpl.this.temp);
                    }

                    void check(final Iterable<ForgeTaskImpl> collection) {
                        final Iterator<ForgeTaskImpl> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final ForgeTaskImpl task = tasks.next();
                            if (task.getOwner().equals(mod)) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(task.getTaskId());
                                }
                            }
                        }
                    }
                }, mySide);
        handle(task, 0L);
        for (ForgeTaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() != -1 && taskPending.getOwner().equals(mod)) {
                taskPending.cancel0();
            }
        }
        for (ForgeTaskImpl runner : runners.values()) {
            if (runner.getOwner().equals(mod)) {
                runner.cancel0();
            }
        }
    }

    public void cancelAllTasks() {
        final ForgeTaskImpl task = new ForgeTaskImpl(
                () -> {
                    Iterator<ForgeTaskImpl> it = runners.values().iterator();
                    while (it.hasNext()) {
                        ForgeTaskImpl task1 = it.next();
                        task1.cancel0();
                        if (task1.isSync()) {
                            it.remove();
                        }
                    }
                    pending.clear();
                    temp.clear();
                }, mySide);
        handle(task, 0L);
        for (ForgeTaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                break;
            }
            taskPending.cancel0();
        }
        for (ForgeTaskImpl runner : runners.values()) {
            runner.cancel0();
        }
    }

    public boolean isCurrentlyRunning(final int taskId) {
        final ForgeTaskImpl task = runners.get(taskId);
        if (task == null || task.isSync()) {
            return false;
        }
        final ForgeAsyncTaskImpl asyncTask = (ForgeAsyncTaskImpl) task;
        synchronized (asyncTask.getWorkers()) {
            return asyncTask.getWorkers().isEmpty();
        }
    }

    public boolean isQueued(final int taskId) {
        if (taskId <= 0) {
            return false;
        }
        for (ForgeTaskImpl task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() == taskId) {
                return task.getPeriod() >= -1L; // The task will run
            }
        }
        ForgeTaskImpl task = runners.get(taskId);
        return task != null && task.getPeriod() >= -1L;
    }

    public List<ForgeWorker> getActiveWorkers() {
        final ArrayList<ForgeWorker> workers = new ArrayList<>();
        for (final ForgeTaskImpl taskObj : runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) {
                continue;
            }
            final ForgeAsyncTaskImpl task = (ForgeAsyncTaskImpl) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    public List<ForgeTask> getPendingTasks() {
        final ArrayList<ForgeTaskImpl> truePending = new ArrayList<>();
        for (ForgeTaskImpl task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() != -1) {
                // -1 is special code
                truePending.add(task);
            }
        }

        final ArrayList<ForgeTask> pending = new ArrayList<>();
        for (ForgeTaskImpl task : runners.values()) {
            if (task.getPeriod() >= -1L) {
                pending.add(task);
            }
        }

        for (final ForgeTaskImpl task : truePending) {
            if (task.getPeriod() >= -1L && !pending.contains(task)) {
                pending.add(task);
            }
        }
        return pending;
    }

    /**
     * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
     */
    public void mainThreadHeartbeat(final int currentTick) {
        this.currentTick = currentTick;
        final List<ForgeTaskImpl> temp = this.temp;
        parsePending();
        while (isReady(currentTick)) {
            final ForgeTaskImpl task = pending.remove();
            if (task.getPeriod() < -1L) {
                if (task.isSync()) {
                    runners.remove(task.getTaskId(), task);
                }
                parsePending();
                continue;
            }
            if (task.isSync()) {
                try {
                    task.run();
                } catch (final Throwable throwable) {
                    PraesidiumEvolutionis.logger.warn(
                            String.format(
                                    "Task #%s for %s generated an exception",
                                    task.getTaskId(),
                                    task.getOwner().getModId()));
                    throwable.printStackTrace();
                }
                parsePending();
            } else {
                debugTail = debugTail.setNext(new ForgeAsyncDebugger(currentTick + RECENT_TICKS, task.getTaskClass()));
                executor.execute(task);
                // We don't need to parse pending
                // (async tasks must live with race-conditions if they attempt to cancel between these few lines of code)
            }
            final long period = task.getPeriod(); // State consistency
            if (period > 0) {
                task.setNextRun(currentTick + period);
                temp.add(task);
            } else if (task.isSync()) {
                runners.remove(task.getTaskId());
            }
        }
        pending.addAll(temp);
        temp.clear();
        debugHead = debugHead.getNextHead(currentTick);
    }

    private void addTask(final ForgeTaskImpl task) {
        final AtomicReference<ForgeTaskImpl> tail = this.tail;
        ForgeTaskImpl tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task)) {
            tailTask = tail.get();
        }
        tailTask.setNext(task);
    }

    private ForgeTaskImpl handle(final ForgeTaskImpl task, final long delay) {
        task.setNextRun(currentTick + delay);
        addTask(task);
        return task;
    }

    private int nextId() {
        return ids.incrementAndGet();
    }

    private void parsePending() {
        ForgeTaskImpl head = this.head;
        ForgeTaskImpl task = head.getNext();
        ForgeTaskImpl lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= -1L) {
                pending.add(task);
                runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all the async calls in CraftScheduler
        // (it prevents race-conditions)
        for (task = head; task != lastTask; task = head) {
            head = task.getNext();
            task.setNext(null);
        }
        this.head = lastTask;
    }

    private boolean isReady(final int currentTick) {
        return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
    }

    @Override
    public String toString() {
        int debugTick = currentTick;
        StringBuilder string = new StringBuilder("Recent tasks from ").append(debugTick - RECENT_TICKS).append('-').append(debugTick).append('{');
        debugHead.debugTo(string);
        return string.append('}').toString();
    }
}
