package io.github.cruciblemc.praesidium_evolutionis.api.scheduler;

import cpw.mods.fml.common.ModContainer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Represents the scheduler API for Praesidium Evolutionis, a Forge environment port of Bukkit's scheduler.
 * <p>
 * This API provides functionality for scheduling tasks to run at specific intervals or after a delay.
 * There are methods available for scheduling tasks to be executed synchronously or asynchronously.
 */
public interface ForgeScheduler {

    /**
     * Schedules a one-time synchronous task to occur after a specified delay.
     *
     * @param mod   The mod that owns the task.
     * @param task  The task to be executed.
     * @param delay The delay in ticks before executing the task.
     * @return The task ID number (-1 if scheduling failed).
     */
    int scheduleSyncDelayedTask(ModContainer mod, Runnable task, long delay);

    /**
     * Schedules a one-time synchronous task to occur as soon as possible.
     *
     * @param mod  The mod that owns the task.
     * @param task The task to be executed.
     * @return The task ID number (-1 if scheduling failed).
     */
    int scheduleSyncDelayedTask(ModContainer mod, Runnable task);

    /**
     * Schedules a repeating synchronous task.
     *
     * @param mod    The mod that owns the task.
     * @param task   The task to be executed.
     * @param delay  The delay in ticks before executing the first repeat.
     * @param period The period in ticks of the task.
     * @return The task ID number (-1 if scheduling failed).
     */
    int scheduleSyncRepeatingTask(ModContainer mod, Runnable task, long delay, long period);

    /**
     * Calls a method on the main thread and returns a Future object.
     * The task will be executed by the main thread that the scheduler belongs to.
     * <p>
     * Note that the Future.get() methods must NOT be called from the main thread.
     * There is typically a latency of at least 10ms until the isDone() method returns true.
     *
     * @param <T>  The callable's return type.
     * @param mod  The mod that owns the task.
     * @param task The task to be executed.
     * @return A Future object related to the task.
     */
    <T> Future<T> callSyncMethod(ModContainer mod, Callable<T> task);

    /**
     * Cancels a task with the specified ID.
     *
     * @param taskId The ID number of the task to be canceled.
     */
    void cancelTask(int taskId);

    /**
     * Cancels all tasks associated with a particular mod.
     *
     * @param mod The mod that owns the tasks to be canceled.
     */
    void cancelTasks(ModContainer mod);

    /**
     * Cancels all tasks in the scheduler.
     */
    void cancelAllTasks();

    /**
     * Checks if a task is currently running.
     * <p>
     * A repeating task might not be running currently but will be running in the future.
     * A task that has finished and does not repeat will not be running ever again.
     * <p>
     * Explicitly, a task is considered running if there exists a thread for it and that thread is alive.
     *
     * @param taskId The ID of the task to check.
     * @return true if the task is currently running, false otherwise.
     */
    boolean isCurrentlyRunning(int taskId);

    /**
     * Checks if a task is queued to be run later.
     * <p>
     * If a repeating task is currently running, it might not be queued now but could be in the future.
     * A task that is not queued and not running will not be queued again.
     *
     * @param taskId The ID of the task to check.
     * @return true if the task is queued to be run, false otherwise.
     */
    boolean isQueued(int taskId);

    /**
     * Returns a list of all active worker threads.
     * <p>
     * This list contains asynchronous tasks that are being executed by separate threads.
     *
     * @return A list of active worker threads.
     */
    List<ForgeWorker> getActiveWorkers();

    /**
     * Returns a list of all pending tasks.
     * <p>
     * The ordering of the tasks is not related to their order of execution.
     *
     * @return A list of pending tasks.
     */
    List<ForgeTask> getPendingTasks();

    /**
     * Runs a task on the next tick.
     *
     * @param mod  The reference to the mod scheduling the task.
     * @param task The task to be run.
     * @return A ForgeTask object that contains the task ID number.
     * @throws IllegalArgumentException if mod is null or task is null.
     */
    ForgeTask runTask(ModContainer mod, Runnable task) throws IllegalArgumentException;

    /**
     * Runs a task asynchronously.
     *
     * <p>When scheduling tasks asynchronously, ensure thread-safety and avoid accessing any API in any mod,
     * Forge, or Minecraft.</p>
     *
     * @param mod  The reference to the mod scheduling the task.
     * @param task The task to be run.
     * @return A ForgeTask object that contains the task ID number.
     * @throws IllegalArgumentException if mod is null or task is null.
     */
    ForgeTask runTaskAsynchronously(ModContainer mod, Runnable task) throws IllegalArgumentException;

    /**
     * Runs a task after a specified number of ticks.
     *
     * @param mod   The reference to the mod scheduling the task.
     * @param task  The task to be run.
     * @param delay The number of ticks to wait before running the task.
     * @return A ForgeTask object that contains the task ID number.
     * @throws IllegalArgumentException if mod is null or task is null.
     */
    ForgeTask runTaskLater(ModContainer mod, Runnable task, long delay) throws IllegalArgumentException;

    /**
     * Runs a task asynchronously after a specified number of ticks.
     *
     * <p>When scheduling tasks asynchronously, ensure thread-safety and avoid accessing any API in any mod,
     * Forge, or Minecraft.</p>
     *
     * @param mod   The reference to the mod scheduling the task.
     * @param task  The task to be run.
     * @param delay The number of ticks to wait before running the task.
     * @return A ForgeTask object that contains the task ID number.
     * @throws IllegalArgumentException if mod is null or task is null.
     */
    ForgeTask runTaskLaterAsynchronously(ModContainer mod, Runnable task, long delay) throws IllegalArgumentException;

    /**
     * Runs a repeating task until cancelled, starting after a specified number of ticks.
     *
     * @param mod    The reference to the mod scheduling the task.
     * @param task   The task to be run.
     * @param delay  The number of ticks to wait before running the task for the first time.
     * @param period The number of ticks to wait between runs.
     * @return A ForgeTask object that contains the task ID number.
     * @throws IllegalArgumentException if mod is null or task is null.
     */
    ForgeTask runTaskTimer(ModContainer mod, Runnable task, long delay, long period) throws IllegalArgumentException;

    /**
     * Runs a repeating task asynchronously until cancelled, starting after a specified number of ticks.
     *
     * <p>When scheduling tasks asynchronously, ensure thread-safety and avoid accessing any API in any mod,
     * Forge, or Minecraft.</p>
     *
     * @param mod    The reference to the mod scheduling the task.
     * @param task   The task to be run.
     * @param delay  The number of ticks to wait before running the task for the first time.
     * @param period The number of ticks to wait between runs.
     * @return A ForgeTask object that contains the task ID number.
     * @throws IllegalArgumentException if mod is null or task is null.
     */
    ForgeTask runTaskTimerAsynchronously(ModContainer mod, Runnable task, long delay, long period) throws IllegalArgumentException;
}