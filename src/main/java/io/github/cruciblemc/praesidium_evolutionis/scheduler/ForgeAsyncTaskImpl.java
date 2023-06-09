package io.github.cruciblemc.praesidium_evolutionis.scheduler;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.ForgeWorker;
import org.apiguardian.api.API;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@API(status = API.Status.INTERNAL)
class ForgeAsyncTaskImpl extends ForgeTaskImpl {

    private final LinkedList<ForgeWorker> workers = new LinkedList<>();
    private final Map<Integer, ForgeTaskImpl> runners;

    ForgeAsyncTaskImpl(final Map<Integer, ForgeTaskImpl> runners, ModContainer owner, final Runnable task, final int id, final long delay, Side side) {
        super(task, owner, id, delay, side);
        this.runners = runners;
    }

    @Override
    public boolean isSync() {
        return false;
    }

    @Override
    public void run() {
        final Thread thread = Thread.currentThread();
        synchronized (workers) {
            if (getPeriod() == -2) {
                // Never continue running after cancelled.
                // Checking this with the lock is important!
                return;
            }
            workers.add(
                    new ForgeWorker() {
                        public Thread getThread() {
                            return thread;
                        }

                        public int getTaskId() {
                            return ForgeAsyncTaskImpl.this.getTaskId();
                        }

                        public ModContainer getOwner() {
                            return ForgeAsyncTaskImpl.this.getOwner();
                        }
                    });
        }
        Throwable thrown = null;
        try {
            super.run();
        } catch (final Throwable t) {
            thrown = t;
            throw new RuntimeException(
                    String.format(
                            "Mod %s generated an exception while executing task %s",
                            getOwner().getModId(),
                            getTaskId()),
                    thrown);
        } finally {
            // Cleanup is important for any async task, otherwise ghost tasks are everywhere
            synchronized (workers) {
                try {
                    final Iterator<ForgeWorker> workers = this.workers.iterator();
                    boolean removed = false;
                    while (workers.hasNext()) {
                        if (workers.next().getThread() == thread) {
                            workers.remove();
                            removed = true; // Don't throw exception
                            break;
                        }
                    }
                    if (!removed) {
                        //noinspection ThrowFromFinallyBlock
                        throw new IllegalStateException(
                                String.format(
                                        "Unable to remove worker %s on task %s for %s",
                                        thread.getName(),
                                        getTaskId(),
                                        getOwner().getModId()),
                                thrown); // We don't want to lose the original exception, if any
                    }
                } finally {
                    if (getPeriod() < 0 && workers.isEmpty()) {
                        // At this spot, we know we are the final async task being executed!
                        // Because we have the lock, nothing else is running or will run because delay < 0
                        runners.remove(getTaskId());
                    }
                }
            }
        }
    }

    LinkedList<ForgeWorker> getWorkers() {
        return workers;
    }

    boolean cancel0() {
        synchronized (workers) {
            // Synchronizing here prevents race condition for a completing task
            setPeriod(-2L);
            if (workers.isEmpty()) {
                runners.remove(getTaskId());
            }
        }
        return true;
    }
}
