package io.github.cruciblemc.praesidium_evolutionis.api.scheduler;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;

/**
 * This abstract class provides a convenient way to handle scheduling tasks using the Forge scheduler.
 * Implement the {@link Runnable} interface and override the {@link Runnable#run()} method with the
 * task logic.
 */
public abstract class ForgeRunnable implements Runnable {
    private int taskId = -1;
    private Side mySide;

    /**
     * Attempts to cancel this task.
     *
     * @throws IllegalStateException if the task was not scheduled yet
     */
    public synchronized void cancel() throws IllegalStateException {
        int id = getTaskId();
        SchedulerManager.getScheduler(mySide).cancelTask(id);
    }

    /**
     * Schedules this task to run on the next tick in the scheduler.
     *
     * @param mod  The reference to the mod scheduling the task.
     * @param side The side to schedule the task on.
     * @return A {@link ForgeTask} that contains the task's ID number.
     * @throws IllegalArgumentException If mod is null or attempted to register in an invalid side.
     * @throws IllegalStateException    If this task was already scheduled.
     * @see ForgeScheduler#runTask(ModContainer, Runnable)
     */
    public synchronized ForgeTask runTask(ModContainer mod, Side side) throws IllegalArgumentException, IllegalStateException {
        checkState();
        return setupId(SchedulerManager.getScheduler(side).runTask(mod, this), side);
    }

    /**
     * Schedules this task to run asynchronously in the scheduler.
     *
     * <p>When scheduling tasks asynchronously, ensure thread-safety and avoid accessing any API in any mod,
     * Forge, or Minecraft.</p>
     *
     * @param mod  The reference to the mod scheduling the task.
     * @param side The side to schedule the task on.
     * @return A {@link ForgeTask} that contains the task's ID number.
     * @throws IllegalArgumentException If mod is null or attempted to register in an invalid side.
     * @throws IllegalStateException    If this task was already scheduled.
     * @see ForgeScheduler#runTaskAsynchronously(ModContainer, Runnable)
     */
    public synchronized ForgeTask runTaskAsynchronously(ModContainer mod, Side side) throws IllegalArgumentException, IllegalStateException {
        checkState();
        return setupId(SchedulerManager.getScheduler(side).runTaskAsynchronously(mod, this), side);
    }

    /**
     * Schedules this task to run after the specified number of ticks in the scheduler.
     *
     * @param mod   The reference to the mod scheduling the task
     * @param side  The side to schedule the task on.
     * @param delay The ticks to wait before running the task.
     * @return A {@link ForgeTask} that contains the task's ID number.
     * @throws IllegalArgumentException If mod is null or attempted to register in an invalid side.
     * @throws IllegalStateException    If this task was already scheduled.
     * @see ForgeScheduler#runTaskLater(ModContainer, Runnable, long)
     */
    public synchronized ForgeTask runTaskLater(ModContainer mod, Side side, long delay) throws IllegalArgumentException, IllegalStateException {
        checkState();
        return setupId(SchedulerManager.getScheduler(side).runTaskLater(mod, this, delay), side);
    }

    /**
     * Schedules this task to run asynchronously after the specified number of ticks in the scheduler.
     *
     * <p>When scheduling tasks asynchronously, ensure thread-safety and avoid accessing any API in any mod,
     * Forge, or Minecraft.</p>
     *
     * @param mod   The reference to the mod scheduling the task
     * @param side  The side to schedule the task on.
     * @param delay The ticks to wait before running the task.
     * @return A {@link ForgeTask} that contains the task's ID number.
     * @throws IllegalArgumentException If mod is null or attempted to register in an invalid side.
     * @throws IllegalStateException    If this task was already scheduled.
     * @see ForgeScheduler#runTaskLaterAsynchronously(ModContainer, Runnable, long)
     */
    public synchronized ForgeTask runTaskLaterAsynchronously(ModContainer mod, Side side, long delay) throws IllegalArgumentException, IllegalStateException {
        checkState();
        return setupId(SchedulerManager.getScheduler(side).runTaskLaterAsynchronously(mod, this, delay), side);
    }

    /**
     * Schedules this task to repeatedly run until cancelled, starting after the specified number
     * of ticks in the scheduler.
     *
     * @param mod    The reference to the mod scheduling the task
     * @param side   The side to schedule the task on.
     * @param delay  The ticks to wait before running the task.
     * @param period The ticks to wait between runs.
     * @return A {@link ForgeTask} that contains the task's ID number.
     * @throws IllegalArgumentException If mod is null or attempted to register in an invalid side.
     * @throws IllegalStateException    If this task was already scheduled.
     * @see ForgeScheduler#runTaskTimer(ModContainer, Runnable, long, long)
     */
    public synchronized ForgeTask runTaskTimer(ModContainer mod, Side side, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        checkState();
        return setupId(SchedulerManager.getScheduler(side).runTaskTimer(mod, this, delay, period), side);
    }

    /**
     * Schedules this task to repeatedly run asynchronously until cancelled, starting after the
     * specified number of ticks in the scheduler.
     *
     * <p>When scheduling tasks asynchronously, ensure thread-safety and avoid accessing any API in any mod,
     * Forge, or Minecraft.</p>
     *
     * @param mod    The reference to the mod scheduling the task
     * @param side   The side to schedule the task on.
     * @param delay  The ticks to wait before running the task.
     * @param period The ticks to wait between runs.
     * @return A {@link ForgeTask} that contains the task's ID number.
     * @throws IllegalArgumentException If mod is null or attempted to register in an invalid side.
     * @throws IllegalStateException    If this task was already scheduled.
     * @see ForgeScheduler#runTaskTimerAsynchronously(ModContainer, Runnable, long, long)
     */
    public synchronized ForgeTask runTaskTimerAsynchronously(ModContainer mod, Side side, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        checkState();
        return setupId(SchedulerManager.getScheduler(side).runTaskTimerAsynchronously(mod, this, delay, period), side);
    }

    /**
     * Gets the task ID for this runnable.
     *
     * @return the task ID that this runnable was scheduled as
     * @throws IllegalStateException if the task was not scheduled yet
     */
    public synchronized int getTaskId() throws IllegalStateException {
        final int id = taskId;
        if (id == -1) {
            throw new IllegalStateException("Not scheduled yet");
        }
        return id;
    }

    private void checkState() {
        if (taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + taskId);
        }
    }

    private ForgeTask setupId(final ForgeTask task, Side side) {
        this.taskId = task.getTaskId();
        this.mySide = side;
        return task;
    }
}