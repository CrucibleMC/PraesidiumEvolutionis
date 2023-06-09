package io.github.cruciblemc.praesidium_evolutionis.mixins.forge.tracking;

import io.github.cruciblemc.praesidium_evolutionis.Tags;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.Identification;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.IdentificationManager;
import io.github.cruciblemc.praesidium_evolutionis.hooks.TrackableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements TrackableEntity {
    private static final String NBT_PREFIX = Tags.MODID + ".owner.";

    Identification ownerIdentification = IdentificationManager.nobody();

    @Override
    public Identification getOwner() {
        return ownerIdentification;
    }

    @Override
    public void setOwner(Identification owner) {
        ownerIdentification = owner;
    }

    @Inject(method = "readFromNBT", at = @At("HEAD"))
    private void readInject(NBTTagCompound tag, CallbackInfo callback) {
        if (IdentificationManager.existsInNbt(tag, NBT_PREFIX)) {
            ownerIdentification = IdentificationManager.readFromNbt(tag, NBT_PREFIX);
        }
    }

    @Inject(method = "writeToNBT", at = @At("HEAD"))
    private void writeInject(NBTTagCompound nbtTagCompound, CallbackInfo callback) {
        ownerIdentification.saveToNbt(nbtTagCompound, NBT_PREFIX);
    }
}
