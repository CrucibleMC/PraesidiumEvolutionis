package io.github.cruciblemc.praesidium_evolutionis.mixins.forge.tracking;

import io.github.cruciblemc.praesidium_evolutionis.hooks.ThrowableProxy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityThrowable.class)
public abstract class MixinEntityThrowable implements ThrowableProxy {
    @Shadow
    private EntityLivingBase thrower;

    @Override
    public void setThrower(EntityLivingBase thrower) {
        this.thrower = thrower;
    }
}
