package io.github.cruciblemc.praesidium_evolutionis;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.relauncher.CoreModManager;
import io.github.cruciblemc.omniconfig.api.OmniconfigAPI;
import io.github.cruciblemc.praesidium_evolutionis.api.ReflectionHelper;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.SchedulerManager;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.TileTracking;
import io.github.cruciblemc.praesidium_evolutionis.config.CommonConfig;
import lombok.SneakyThrows;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]", acceptableRemoteVersions = "*")
public class PraesidiumEvolutionis {

    public static final Logger logger = LogManager.getLogger(Tags.MODID);
    public static final boolean bukkit = ReflectionHelper.doesClassExist("org.bukkit.Bukkit");

    @Mod.Instance(Tags.MODID)
    private static PraesidiumEvolutionis mod = null;

    private static ModContainer myContainer;

    public PraesidiumEvolutionis() {
        OmniconfigAPI.registerAnnotationConfig(CommonConfig.class);
    }

    @SneakyThrows
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!TileTracking.isAvailable()) {
            logger.warn(" ");
            logger.warn("Tile tracking is not available!");
            logger.warn("Please install UniMixins to allow tiles to be tracked.");
            logger.warn(" ");
        } else {
            MinecraftForge.EVENT_BUS.register(TileTrackingListener.INSTANCE);
        }

        SchedulerManager.init();

        if (FMLForgePlugin.RUNTIME_DEOBF || CommonConfig.debugging_enabled) {
            var devEvents = new DevEventListener();
            MinecraftForge.EVENT_BUS.register(devEvents);
            FMLCommonHandler.instance().bus().register(devEvents);
        }
    }

    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            SchedulerManager.internalServerStopped();
        } else {
            SchedulerManager.shutdown();
        }
    }

    @NotNull
    public static PraesidiumEvolutionis getMod() {
        return mod;
    }

    @NotNull
    public static ModContainer getContainer() {
        if (myContainer == null) {
            myContainer = Loader.instance().getIndexedModList().get(Tags.MODID);
        }
        return myContainer;
    }

    public static UUID offlineUUID(String playerName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }
}
