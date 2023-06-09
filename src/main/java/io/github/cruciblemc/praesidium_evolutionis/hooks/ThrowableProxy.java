package io.github.cruciblemc.praesidium_evolutionis.hooks;

import net.minecraft.entity.EntityLivingBase;

public interface ThrowableProxy {
    void setThrower(EntityLivingBase thrower);
}
