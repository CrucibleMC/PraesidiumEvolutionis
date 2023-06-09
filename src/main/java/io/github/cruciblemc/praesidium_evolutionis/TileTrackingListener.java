package io.github.cruciblemc.praesidium_evolutionis;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.github.cruciblemc.praesidium_evolutionis.api.fakeplayer.BetterFakePlayer;
import io.github.cruciblemc.praesidium_evolutionis.api.scheduler.SchedulerManager;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.Identification;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.IdentificationManager;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.TileTracking;
import io.github.cruciblemc.praesidium_evolutionis.config.CommonConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

public class TileTrackingListener {
    static final TileTrackingListener INSTANCE = new TileTrackingListener();

    private TileTrackingListener() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void placeEvent(BlockEvent.PlaceEvent event) {
        if (CommonConfig.tracking_enabled) {
            if (event.player instanceof BetterFakePlayer) {
                Identification owner = ((BetterFakePlayer) event.player).getOwner();
                SchedulerManager.getServerScheduler().runTask(PraesidiumEvolutionis.getContainer(),
                        new SetOwner(event.x, event.y, event.z, owner, event.world));

            } else if (!(event.player instanceof FakePlayer)) {
                SchedulerManager.getServerScheduler().runTask(PraesidiumEvolutionis.getContainer(),
                        new SetOwner(event.x, event.y, event.z, IdentificationManager.fromPlayer(event.player), event.world));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void multiPlaceEvent(BlockEvent.MultiPlaceEvent event) {
        if (CommonConfig.tracking_enabled) {
            if (event.player instanceof BetterFakePlayer) {
                Identification owner = ((BetterFakePlayer) event.player).getOwner();

                event.getReplacedBlockSnapshots().forEach(block ->
                        SchedulerManager.getServerScheduler().runTask(PraesidiumEvolutionis.getContainer(),
                                new SetOwner(block.x, block.y, block.z, owner, event.world)));
            } else if (!(event.player instanceof FakePlayer)) {
                event.getReplacedBlockSnapshots().forEach(block -> {
                    var ident = IdentificationManager.fromPlayer(event.player);
                    SchedulerManager.getServerScheduler().runTask(PraesidiumEvolutionis.getContainer(),
                            new SetOwner(block.x, block.y, block.z, ident, event.world));
                });
            }
        }
    }
}

class SetOwner implements Runnable {
    private final int x;
    private final int y;
    private final int z;
    private final Identification player;
    private final World world;

    SetOwner(int x, int y, int z, Identification player, World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = player;
        this.world = world;
    }

    @Override
    public void run() {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null) {
            TileTracking.setOwner(tile, player);
        }
    }
}
