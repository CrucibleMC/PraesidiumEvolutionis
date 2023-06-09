package io.github.cruciblemc.praesidium_evolutionis.api.fakeplayer;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.github.cruciblemc.praesidium_evolutionis.PraesidiumEvolutionis;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.Identification;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.IdentificationManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * This class provides methods for creating and configuring fake players without using many resources.
 * Fake players returned by this class are reused and may have their information modified at any time.
 * <p>
 * To avoid, do not keep references to the returned fake player instances.
 */
public class FakePlayerManager {
    private static final GameProfile GENERIC_PROFILE = new GameProfile(PraesidiumEvolutionis.offlineUUID("[GenericFakePlayer]"), "[GenericFakePlayer]");
    private static final ConcurrentMap<String, BetterFakePlayer> CACHE = new MapMaker()
            .weakValues()
            .makeMap();

    private static SoftReference<BetterFakePlayer> genericFakePlayer = new SoftReference<>(null);

    /**
     * Get or create a fake player.
     * Please do not keep or hold the instance of the fake player, fake player instances are reused,
     * it's information may change after use including the world and position.
     * <p>
     * The resulting fake player will have the name wrapped around "[]".
     *
     * @param world - the world the fake player will be.
     * @param pos   - the position the fake player will be.
     * @param name  - the name of the fake player.
     * @return A configured fake player.
     */
    @NotNull
    public static BetterFakePlayer getAndConfigure(WorldServer world, ChunkCoordinates pos, String name) {
        BetterFakePlayer fakePlayer = CACHE.get(name);
        if (fakePlayer == null) {
            String fName = "[" + name + "]";
            GameProfile profile = new GameProfile(PraesidiumEvolutionis.offlineUUID(fName), fName);
            fakePlayer = new BetterFakePlayer(world, profile, IdentificationManager.nobody());
            CACHE.put(name, fakePlayer);
        }
        fakePlayer.worldObj = world;
        fakePlayer.setFakePosition(pos);
        return fakePlayer;
    }

    /**
     * Get or create a fake player.
     * Please do not keep or hold the instance of the fake player, fake player instances are reused,
     * it's information may change after use including the world and position.
     * <p>
     * The resulting fake player will have the same name of the owner wrapped around "[]".
     *
     * @param world - the world the fake player will be.
     * @param pos   - the position the fake player will be.
     * @param owner - the user owning this fake player with may receive proxied chat messages and help with tracking.
     * @return a configured fake player, the object may be reused and reconfigured by other calls, prefer to call this method again when a fake player is needed.
     */
    public static BetterFakePlayer getAndConfigure(WorldServer world, ChunkCoordinates pos, Identification owner) {
        if (owner.equals(IdentificationManager.nobody())) {
            return getAndConfigure(world, pos);
        }
        BetterFakePlayer fakePlayer = CACHE.get(owner.getPlayerName());
        if (fakePlayer == null) {
            String fName = "[" + owner.getPlayerName() + "]";
            GameProfile profile = new GameProfile(PraesidiumEvolutionis.offlineUUID(fName), fName);
            fakePlayer = new BetterFakePlayer(world, profile, owner);
            CACHE.put(owner.getPlayerName(), fakePlayer);
        }
        fakePlayer.worldObj = world;
        fakePlayer.setFakePosition(pos);
        return fakePlayer;
    }

    /**
     * Get or create a fake player.
     * Please do not keep or hold the instance of the fake player, fake player instances are reused,
     * it's information may change after use including the world and position.
     * <p>
     * Fake players returned by this method will always have the name "[GenericFakePlayer]" and will always be owned by nobody.
     *
     * @param world the world the fake player will be.
     * @param pos   the position the fake player will be.
     * @return A configured fake player, the object may be reused and reconfigured by other calls, prefer to call this method again when a fake player is needed.
     */
    public static BetterFakePlayer getAndConfigure(WorldServer world, ChunkCoordinates pos) {
        BetterFakePlayer player = genericFakePlayer.get();
        if (player == null) {
            player = new BetterFakePlayer(world, GENERIC_PROFILE, IdentificationManager.nobody());
            genericFakePlayer = new SoftReference<>(player);
        }
        player.worldObj = world;
        player.setFakePosition(pos);
        return player;
    }

    /**
     * Allows you to use a functional style way to run some code with a fake player.
     *
     * @param world    the world the fake player will be.
     * @param pos      the position the fake player will be.
     * @param consumer the consumer that will accept the fake player.
     */
    public static void withFakePlayer(WorldServer world, ChunkCoordinates pos, Consumer<EntityPlayerMP> consumer) {
        BetterFakePlayer fake = getAndConfigure(world, pos);
        consumer.accept(fake);
        fake.worldObj = null;
    }

    /**
     * Allows you to use a functional style way to run some code with a fake player.
     *
     * @param world    the world the fake player will be.
     * @param pos      the position the fake player will be.
     * @param owner    the owner of the fake player.
     * @param consumer the consumer that will accept the fake player.
     */
    public static void withFakePlayer(WorldServer world, ChunkCoordinates pos, Identification owner, Consumer<EntityPlayerMP> consumer) {
        BetterFakePlayer fake = getAndConfigure(world, pos, owner);
        consumer.accept(fake);
        fake.worldObj = null;
    }

    /**
     * Allows you to use a functional style way to run some code with a fake player.
     *
     * @param world    the world the fake player will be.
     * @param pos      the position the fake player will be.
     * @param name     the fake player name.
     * @param consumer the consumer that will accept the fake player.
     */
    public static void withFakePlayer(WorldServer world, ChunkCoordinates pos, String name, Consumer<EntityPlayerMP> consumer) {
        BetterFakePlayer fake = getAndConfigure(world, pos, name);
        consumer.accept(fake);
        fake.worldObj = null;
    }
}
