package io.github.cruciblemc.praesidium_evolutionis.api.scheduler;

import cpw.mods.fml.common.ModContainer;

/**
 * Represents a worker thread for the scheduler. Provides information about
 * the Thread object for the task, the owner of the task, and the taskId.
 * <p>
 * Workers are used to execute asynchronous tasks.
 */
public interface ForgeWorker {

    /**
     * Returns the taskId for the task being executed by this worker.
     *
     * @return The task ID number.
     */
    int getTaskId();

    /**
     * Returns the Thread object for the worker.
     *
     * @return The Thread object for the worker.
     */
    Thread getThread();

    /**
     * Returns the ModContainer that owns this task.
     *
     * @return The ModContainer that owns the task.
     */
    ModContainer getOwner();
}