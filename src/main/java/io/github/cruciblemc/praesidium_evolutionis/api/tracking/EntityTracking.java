package io.github.cruciblemc.praesidium_evolutionis.api.tracking;

import io.github.cruciblemc.praesidium_evolutionis.hooks.ThrowableProxy;
import io.github.cruciblemc.praesidium_evolutionis.hooks.TrackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for entity tracking operations.
 */
public final class EntityTracking {

    private EntityTracking() {
        // Sealed class
    }

    /**
     * Checks if entity tracking is available.
     *
     * @return true if entities are trackable, false otherwise.
     */
    public static boolean isAvailable() {
        return TrackableEntity.class.isAssignableFrom(Entity.class) &&
                ThrowableProxy.class.isAssignableFrom(EntityThrowable.class);
    }

    /**
     * Attempts to get the owner of an entity.
     * When tracking is unavailable, it will always return nobody.
     *
     * @param entity The entity to get the owner from.
     * @return The entity owner.
     */
    @NotNull
    public static Identification getOwner(@NotNull Entity entity) {
        if (entity instanceof TrackableEntity) {
            return ((TrackableEntity) entity).getOwner();
        } else {
            return IdentificationManager.nobody();
        }
    }

    /**
     * Attempts to set the owner of an entity.
     * This operation will have no effect if tracking is unavailable.
     *
     * @param entity The entity to set the owner for.
     * @param owner  The new entity owner.
     */
    public static void setOwner(@NotNull Entity entity, @NotNull Identification owner) {
        if (entity instanceof TrackableEntity) {
            ((TrackableEntity) entity).setOwner(owner);
        }
    }

    /**
     * Attempts to set the thrower of a throwable entity.
     * This operation will have no effect if tracking is unavailable.
     *
     * @param entity  The throwable entity.
     * @param thrower The entity player who threw the entity.
     */
    public static void setThrower(@NotNull EntityThrowable entity, @NotNull EntityPlayer thrower) {
        if (entity instanceof ThrowableProxy) {
            ((ThrowableProxy) entity).setThrower(thrower);
        }
    }
}
