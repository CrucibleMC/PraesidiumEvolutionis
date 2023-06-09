package io.github.cruciblemc.praesidium_evolutionis;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;

public class DevEventListener {
    @SubscribeEvent
    public void dumpTileData(PlayerInteractEvent e) {
        var item = e.entityPlayer.getHeldItem();
        if (item != null && Objects.equals(item.getItem(), Items.stick)) {
            var tile = e.world.getTileEntity(e.x, e.y, e.z);
            if (tile != null) {
                var nbt = new NBTTagCompound();
                tile.writeToNBT(nbt);
                e.entityPlayer.addChatMessage(new ChatComponentText(nbt.toString()));
            }
        }
    }
}
