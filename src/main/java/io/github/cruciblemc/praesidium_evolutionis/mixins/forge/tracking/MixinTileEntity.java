package io.github.cruciblemc.praesidium_evolutionis.mixins.forge.tracking;

import io.github.cruciblemc.praesidium_evolutionis.Tags;
import io.github.cruciblemc.praesidium_evolutionis.api.fakeplayer.FakePlayerManager;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.Identification;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.IdentificationManager;
import io.github.cruciblemc.praesidium_evolutionis.hooks.TrackableTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements TrackableTileEntity {
    private static final String NBT_PREFIX = Tags.MODID + ".owner.";

    @Shadow
    public int xCoord;
    @Shadow
    public int yCoord;
    @Shadow
    public int zCoord;

    Identification ownerIdentification = IdentificationManager.nobody();

    @Shadow
    public abstract void markDirty();

    @Shadow
    public abstract World getWorld();

    @Override
    public Identification getOwner() {
        return ownerIdentification;
    }

    @Override
    public void setOwner(Identification owner) {
        ownerIdentification = owner;
        markDirty();
    }

    @Override
    public EntityPlayer getFakePlayer() {
        return FakePlayerManager.getAndConfigure((WorldServer) getWorld(),
                new ChunkCoordinates(xCoord, yCoord, zCoord),
                ownerIdentification);
    }

    @Override
    public void withFakePlayer(Consumer<EntityPlayerMP> consumer) {
        FakePlayerManager.withFakePlayer((WorldServer) getWorld(),
                new ChunkCoordinates(xCoord, yCoord, zCoord),
                ownerIdentification, consumer);
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
