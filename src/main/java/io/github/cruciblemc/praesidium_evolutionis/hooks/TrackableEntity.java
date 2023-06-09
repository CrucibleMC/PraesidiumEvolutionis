package io.github.cruciblemc.praesidium_evolutionis.hooks;

import io.github.cruciblemc.praesidium_evolutionis.api.tracking.Identification;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.function.Consumer;

public interface TrackableEntity {

    Identification getOwner();

    void setOwner(Identification owner);

}
