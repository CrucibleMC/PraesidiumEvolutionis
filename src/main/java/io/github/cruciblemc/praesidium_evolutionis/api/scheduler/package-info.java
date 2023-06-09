/**
 * This package contains the scheduler API for Praesidium Evolutionis, a Forge environment port of Bukkit's scheduler.
 * <p>
 * The scheduler API provides functionality for scheduling tasks to run at specific intervals or after a delay.
 * There are two schedulers available, each running at the start of a tick on its respective side (server or client).
 * <p>
 * To obtain a scheduler, use the {@link io.github.cruciblemc.praesidium_evolutionis.api.scheduler.SchedulerManager} class.
 * <p>
 * To obtain your mod container you may use the following code:
 * <pre>{@code
 * String modId = "mymod";
 * cpw.mods.fml.common.ModContainer myMod = cpw.mods.fml.common.Loader.instance().getIndexedModList().get(modId);
 * }</pre>
 * <p>
 * If you are using a coremod, you may use your DummyModContainer instance.
 */
@API(status = API.Status.EXPERIMENTAL)
package io.github.cruciblemc.praesidium_evolutionis.api.scheduler;

import org.apiguardian.api.API;