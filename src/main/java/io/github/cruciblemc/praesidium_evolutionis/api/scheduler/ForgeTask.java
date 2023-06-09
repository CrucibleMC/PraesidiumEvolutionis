package io.github.cruciblemc.praesidium_evolutionis.api.scheduler;

import cpw.mods.fml.common.ModContainer;

/**
 * Represents a task being executed by the scheduler.
 */
public interface ForgeTask {

    /**
     * Returns the taskId for the task.
     *
     * @return The task ID number.
     */
    int getTaskId();

    /**
     * Returns true if the task is a synchronous task.
     *
     * @return true if the task is executed by the main thread.
     */
    boolean isSync();

    /**
     * Attempts to cancel this task.
     */
    void cancel();

    /**
     * Returns the ModContainer that owns this task.
     *
     * @return The ModContainer that owns the task.
     */
    ModContainer getOwner();
}