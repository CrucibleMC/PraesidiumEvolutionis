package io.github.cruciblemc.praesidium_evolutionis.api.tracking;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.github.cruciblemc.praesidium_evolutionis.PraesidiumEvolutionis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.UsernameCache;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages instances of Identification for owner tracking across the APIs.
 * This class provides methods to retrieve and cache user identifications from NBTTagCompound and GameProfile.
 */
public final class IdentificationManager {
    // TODO: Eh, is this actually a good way to make cache? Ideally once all trackable tiles tiles are unloaded, the
    //  Identification should be gone from the cache.
    private static final ConcurrentMap<UUID, Identification> IDENTIFICATION_CACHE = new MapMaker()
            .weakValues()
            .makeMap();

    private IdentificationManager() {
        // Sealed class
    }

    /**
     * Checks if an NBTTagCompound contains user identification.
     *
     * @param nbt    The NBTTagCompound to check.
     * @param prefix The prefix to use for the NBT tag keys.
     * @return true if the tag contains user identification, false otherwise.
     */
    public static boolean existsInNbt(@NotNull NBTTagCompound nbt, String prefix) {
        return nbt.hasKey(prefix + "uuid") || nbt.hasKey(prefix + "name");
    }

    /**
     * Reads a {@link Identification} from an {@link NBTTagCompound}.
     *
     * @param nbt    The NBTTagCompound to read from.
     * @param prefix The prefix to use for the NBT tag keys.
     * @return An instance of Identification containing player information. If the given data is invalid,
     * it returns {@link #nobody()}.
     */
    public static @NotNull Identification readFromNbt(@NotNull NBTTagCompound nbt, @NotNull String prefix) {
        String name = nbt.getString(prefix + "name");
        if (nobody().getPlayerName().equals(name))
            return nobody();
        try {
            String uuidString = nbt.getString(prefix + "uuid");
            UUID uuid = UUID.fromString(uuidString);
            Identification identification = IDENTIFICATION_CACHE.get(uuid);

            if (identification == null) {
                String lastKnownName = UsernameCache.getLastKnownUsername(uuid);
                if (lastKnownName != null) {
                    name = lastKnownName;
                }
                identification = name.isEmpty() ? new Identification(uuid) : new Identification(uuid, name);
                IDENTIFICATION_CACHE.put(identification.getUuid(), identification);
            }

            return identification;
        } catch (IllegalArgumentException e) {
            // Bad uuid, let's try to get it from the name instead
            if (!name.isEmpty()) {
                Identification identification = findByName(name);
                if (identification == null) { // No user found, we will have improvised
                    UUID realUuid = null;
                    for (Map.Entry<UUID, String> entry : UsernameCache.getMap().entrySet()) {
                        if (entry.getValue().equals(name)) {
                            realUuid = entry.getKey();
                            break;
                        }
                    }
                    if (realUuid == null) {
                        // We will have to go with an offline player, let's not cache it.
                        identification = new Identification(PraesidiumEvolutionis.offlineUUID(name), name);
                    } else {
                        identification = new Identification(realUuid, name);
                        IDENTIFICATION_CACHE.put(identification.getUuid(), identification);
                    }
                }
                return identification;
            } else {
                // Invalid data from the nbt, return nobody
                return nobody();
            }
        }
    }

    /**
     * Gets the identification of a player.
     *
     * @param player The player to get the user identification from.
     * @return The identification of the player.
     */
    public static @NotNull Identification fromPlayer(@NotNull EntityPlayer player) {
        return fromProfile(player.getGameProfile());
    }

    /**
     * Gets the identification from a complete {@link GameProfile}.
     *
     * @param profile The game profile to get the identification from.
     * @return The identification from the game profile.
     * @throws IllegalArgumentException if the game profile is incomplete.
     */
    public static @NotNull Identification fromProfile(@NotNull GameProfile profile) {
        if (!profile.isComplete())
            throw new IllegalArgumentException("Cannot get a user from an incomplete game profile");
        Identification identification = IDENTIFICATION_CACHE.get(profile.getId());
        if (identification == null) {
            identification = new Identification(profile.getId(), profile.getName());
            IDENTIFICATION_CACHE.put(identification.getUuid(), identification);
        }
        return identification;
    }

    /**
     * Tries to find an identification with the given UUID in the cache.
     *
     * @param uuid The UUID of the player.
     * @return The identification if found, null otherwise.
     */
    public static @Nullable Identification findByUuid(UUID uuid) {
        return IDENTIFICATION_CACHE.get(uuid);
    }

    /**
     * Tries to find an identification with the given name in the cache.
     *
     * @param name The name of the player.
     * @return The identification if found, null otherwise.
     */
    public static @Nullable Identification findByName(String name) {
        for (Identification identification : IDENTIFICATION_CACHE.values()) {
            if (identification.getPlayerName().equals(name))
                return identification;
        }
        return null;
    }

    /**
     * Returns a singleton instance of an identification representing nobody.
     *
     * @return The identification representing nobody.
     */
    public static @NotNull Identification nobody() {
        return Identification.nobody;
    }
}
