package io.github.cruciblemc.praesidium_evolutionis.api.tracking;

import io.github.cruciblemc.praesidium_evolutionis.api.fakeplayer.FakePlayerManager;
import io.github.cruciblemc.praesidium_evolutionis.hooks.TrackableTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Utility class for tile tracking operations.
 */
public final class TileTracking {
    private TileTracking() {
    }

    /**
     * Checks if tile tracking is available.
     *
     * @return true if tiles are trackable, false otherwise.
     */
    public static boolean isAvailable() {
        return TrackableTileEntity.class.isAssignableFrom(TileEntity.class);
    }

    /**
     * Attempts to get the owner of a tile.
     * When tracking is unavailable, it will always return nobody.
     *
     * @param tile The tile to get the owner from.
     * @return The tile owner.
     */
    @NotNull
    public static Identification getOwner(@NotNull TileEntity tile) {
        if (tile instanceof TrackableTileEntity) {
            return ((TrackableTileEntity) tile).getOwner();
        } else {
            return IdentificationManager.nobody();
        }
    }

    /**
     * Attempts to set the owner of a tile.
     * This operation will have no effect if tracking is unavailable.
     *
     * @param tile  The tile to set the owner for.
     * @param owner The new tile owner.
     */
    public static void setOwner(@NotNull TileEntity tile, @NotNull Identification owner) {
        if (tile instanceof TrackableTileEntity) {
            ((TrackableTileEntity) tile).setOwner(owner);
        }
    }

    /**
     * Configures a fake player with {@link FakePlayerManager} based on the tile entity information.
     * If the tile has no owner or tracking is unavailable, it will return a generic fake player.
     *
     * @param tile The tile to get the fake player from.
     * @return The fake player associated with the tile.
     */
    @NotNull
    public static EntityPlayer getFakePlayer(@NotNull TileEntity tile) {
        if (tile instanceof TrackableTileEntity) {
            return ((TrackableTileEntity) tile).getFakePlayer();
        } else {
            return FakePlayerManager.getAndConfigure((WorldServer) tile.getWorld(),
                    new ChunkCoordinates(tile.xCoord, tile.yCoord, tile.zCoord));
        }
    }

    /**
     * Executes a consumer function with a fake player.
     * If the tile has no owner or tracking is unavailable, it will use a generic fake player.
     *
     * @param tile     The tile entity to configure the fake player from. It will fallback to a generic fake player if tracking is not enabled.
     * @param consumer The consumer function that accepts the fake player.
     */
    public static void withFakePlayer(@NotNull TileEntity tile, Consumer<EntityPlayerMP> consumer) {
        if (tile instanceof TrackableTileEntity) {
            ((TrackableTileEntity) tile).withFakePlayer(consumer);
        } else {
            FakePlayerManager.withFakePlayer((WorldServer) tile.getWorld(),
                    new ChunkCoordinates(tile.xCoord, tile.yCoord, tile.zCoord),
                    consumer);
        }
    }
}
