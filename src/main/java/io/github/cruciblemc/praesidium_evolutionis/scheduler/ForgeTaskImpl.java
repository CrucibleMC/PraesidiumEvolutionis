package io.github.cruciblemc.praesidium_evolutionis.scheduler;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.ForgeTask;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.SchedulerManager;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public class ForgeTaskImpl implements ForgeTask, Runnable {

    private final Runnable task;
    private final int id;
    private final ModContainer owner;
    private final Side mySide;
    private volatile ForgeTaskImpl next = null;
    /**
     * -1 means no repeating <br>
     * -2 means cancel <br>
     * -3 means processing for Future <br>
     * -4 means done for Future <br>
     * Never 0 <br>
     * >0 means number of ticks to wait between each execution
     */
    private volatile long period;
    private long nextRun;

    ForgeTaskImpl(Side side) {
        this(null, null, -1, -1, side);
    }

    ForgeTaskImpl(final Runnable task, Side side) {
        this(task, null, -1, -1, side);
    }

    ForgeTaskImpl(final Runnable task, ModContainer owner, final int id, final long period, Side side) {
        this.task = task;
        this.owner = owner;
        this.id = id;
        this.period = period;
        this.mySide = side;
    }

    public final int getTaskId() {
        return id;
    }

    public boolean isSync() {
        return true;
    }

    public void run() {
        task.run();
    }

    long getPeriod() {
        return period;
    }

    void setPeriod(long period) {
        this.period = period;
    }

    long getNextRun() {
        return nextRun;
    }

    void setNextRun(long nextRun) {
        this.nextRun = nextRun;
    }

    ForgeTaskImpl getNext() {
        return next;
    }

    void setNext(ForgeTaskImpl next) {
        this.next = next;
    }

    public Class<? extends Runnable> getTaskClass() {
        return task.getClass();
    }

    public void cancel() {
        SchedulerManager.getScheduler(mySide).cancelTask(id);
    }

    @Override
    public ModContainer getOwner() {
        return owner;
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean cancel0() {
        setPeriod(-2L);
        return true;
    }
}