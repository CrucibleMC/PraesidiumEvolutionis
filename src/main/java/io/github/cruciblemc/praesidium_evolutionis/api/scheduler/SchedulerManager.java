package io.github.cruciblemc.praesidium_evolutionis.api.scheduler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import io.github.cruciblemc.praesidium_evolutionis.scheduler.ForgeSchedulerImpl;
import net.minecraft.client.Minecraft;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to manage server and client side schedulers.
 */
public final class SchedulerManager {
    private static final ForgeScheduler SERVER_SCHEDULER;
    @Nullable
    private static final ForgeScheduler CLIENT_SCHEDULER;

    static {
        SERVER_SCHEDULER = new ForgeSchedulerImpl(Side.SERVER);
        if (FMLCommonHandler.instance().getSide().isClient())
            CLIENT_SCHEDULER = new ForgeSchedulerImpl(Side.CLIENT);
        else
            CLIENT_SCHEDULER = null;
    }

    private SchedulerManager() {
        // Sealed class
    }

    /**
     * Get the scheduler for the specified side.
     *
     * @param side The side to get the scheduler for.
     * @return The scheduler for the specified side.
     * @throws IllegalArgumentException If the scheduler for the specified side is not available.
     *                                  See {@link #getClientScheduler()} and {@link #getServerScheduler()} for more information.
     */
    public static ForgeScheduler getScheduler(Side side) {
        return switch (side) {
            case CLIENT -> getClientScheduler();
            case SERVER -> getServerScheduler();
        };
    }

    /**
     * Get the scheduler for the client side.
     * The client scheduler will run tasks even if the client is not in a world.
     *
     * @return The scheduler for the client side.
     * @throws IllegalStateException If running on a dedicated server.
     */
    public static ForgeScheduler getClientScheduler() {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            throw new IllegalStateException("Cannot use the client scheduler in a dedicated server.");
        }
        return CLIENT_SCHEDULER;
    }

    /**
     * Get the scheduler for the server side.
     *
     * @return The scheduler for the server side.
     * @throws IllegalStateException If running on the client and the integrated server is not running.
     */
    public static ForgeScheduler getServerScheduler() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
                throw new IllegalStateException("Integrated server is not running.");
            }
        }
        return SERVER_SCHEDULER;
    }

    /**
     * Internal method, do not call.
     */
    @API(status = API.Status.INTERNAL)
    public static void init() {
        FMLCommonHandler.instance().bus().register(SERVER_SCHEDULER);
        if (CLIENT_SCHEDULER != null)
            FMLCommonHandler.instance().bus().register(CLIENT_SCHEDULER);
    }

    /**
     * Internal method, do not call.
     * Resets the state of the server scheduler when the internal server is stopped.
     */
    @API(status = API.Status.INTERNAL)
    public static void internalServerStopped() {
        ((ForgeSchedulerImpl) SERVER_SCHEDULER).resetState();
    }

    /**
     * Internal method, do not call.
     */
    @API(status = API.Status.INTERNAL)
    public static void shutdown() {
        ((ForgeSchedulerImpl) SERVER_SCHEDULER).resetState();
        if (CLIENT_SCHEDULER != null)
            ((ForgeSchedulerImpl) SERVER_SCHEDULER).resetState();
    }
}
