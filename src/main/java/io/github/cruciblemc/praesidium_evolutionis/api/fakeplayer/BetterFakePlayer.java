package io.github.cruciblemc.praesidium_evolutionis.api.fakeplayer;

import com.mojang.authlib.GameProfile;
import io.github.cruciblemc.praesidium_evolutionis.api.tracking.Identification;
import io.github.cruciblemc.praesidium_evolutionis.config.CommonConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A custom fake player implementation which has better tracking methods for who is the owner of it.
 */
public class BetterFakePlayer extends FakePlayer {
    private final Identification owner;
    private ChunkCoordinates fakePos = new ChunkCoordinates(0, 0, 0);

    public BetterFakePlayer(WorldServer world, GameProfile name, Identification owner) {
        super(world, name);
        this.owner = owner;
    }

    /**
     * Set the position of the fake player in the world.
     *
     * @param x The x axi.
     * @param y The y axi.
     * @param z The z axi.
     */
    public void setFakePosition(int x, int y, int z) {
        setFakePosition(new ChunkCoordinates(x, y, z));
    }

    /**
     * Set the position of the fake player in the world.
     *
     * @param pos - the position of the player in chunk coordinates.
     */
    public void setFakePosition(ChunkCoordinates pos) {
        fakePos = Objects.requireNonNull(pos);
        setPosition(fakePos.posX, fakePos.posY, fakePos.posZ);
    }

    @Override
    public ChunkCoordinates getCommandSenderPosition() {
        return fakePos;
    }

    @Override
    public void addChatComponentMessage(IChatComponent message) {
        if (CommonConfig.tracking_proxyFakePlayerMessages) {
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(owner.getPlayerName());
            if (player != null) {
                ChatComponentText toSend = new ChatComponentText(
                        String.format("§e%s at §bx:%s y:%s z:%s§r ",
                                getCommandSenderName(),
                                fakePos.posX,
                                fakePos.posY,
                                fakePos.posZ));
                toSend.appendSibling(message);
            }
        }
    }

    /**
     * Gets the user that originated this fake player, useful for tracking and logging.
     *
     * @return The owner of this fake player.
     */
    @NotNull
    public Identification getOwner() {
        return owner;
    }
}
