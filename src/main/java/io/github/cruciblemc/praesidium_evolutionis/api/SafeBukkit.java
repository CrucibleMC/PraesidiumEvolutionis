package io.github.cruciblemc.praesidium_evolutionis.api;

import io.github.crucible.api.CrucibleAPI;
import io.github.cruciblemc.praesidium_evolutionis.PraesidiumEvolutionis;
import io.github.cruciblemc.praesidium_evolutionis.config.ServerConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.UserListOpsEntry;
import org.apiguardian.api.API;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Interface API to safely interact with Bukkit without risking unwanted classloading and have safe fallbacks
 * for non-Bukkit environments.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface SafeBukkit {
    /**
     * Reference to the implementation of SafeBukkit.
     */
    @NotNull
    SafeBukkit REF = Provider.get();

    /**
     * Checks if we are in a Bukkit environment.
     *
     * @return true if Bukkit is present, false otherwise.
     */
    boolean hasBukkit();

    /**
     * Checks if we are in a Crucible environment.
     *
     * @return true if Crucible is present, false otherwise.
     */
    boolean hasCrucible();

    /**
     * Checks if an item stack is from Bukkit.
     *
     * @param stack The item stack to check.
     * @return true if the stack is an {@link org.bukkit.inventory.ItemStack} instance, false otherwise.
     */
    boolean isBukkitStack(@NotNull Object stack);

    /**
     * Shortcut to calling {@link #getPermissionInteger(EntityPlayerMP, String, int)} with fallbackOpLevel 4.
     *
     * @param player The player to check.
     * @param node   The permission node to check.
     * @return true if the player has the permission node.
     */
    boolean hasPermission(@NotNull EntityPlayerMP player, @NotNull String node);

    /**
     * Attempts to checks if a player has a permission node. If the current environment lacks a permission system,
     * it falls back to using an operator level similar to commands, where 0 means everyone has the permission,
     * and 4 means only server operators have the permission (or require cheats enabled in single-player).
     *
     * @param player          The player to check.
     * @param node            The permission node to check.
     * @param fallbackOpLevel The fallback operator level.
     * @return true if the player has the permission node.
     */
    boolean hasPermission(@NotNull EntityPlayerMP player, @NotNull String node, int fallbackOpLevel);

    /**
     * Attempts to get an integer value provided in a permission node. If no node is found or if
     * the current environment lacks a permission system, it returns the default value.
     *
     * @param player        The player to check.
     * @param node          The permission node to check.
     * @param defaultValue  The default value to return if no node is found or if the current environment lacks a permission system.
     * @return The integer value from the permission node or the default value.
     */
    int getPermissionInteger(@NotNull EntityPlayerMP player, @NotNull String node, int defaultValue);

    /**
     * Attempts to get a string value provided in a permission node. If no node is found or if
     * the current environment lacks a permission system, it returns the default value.
     *
     * @param player        The player to check.
     * @param node          The permission node to check.
     * @param defaultValue  The default value to return if no node is found or if the current environment lacks a permission system.
     * @return The string value from the permission node or the default value.
     */
    @NotNull String getPermissionString(@NotNull EntityPlayerMP player, @NotNull String node, @NotNull String defaultValue);

    /**
     * Copies and converts a Bukkit stack to a Minecraft stack.
     *
     * @param bukkitStack An {@link org.bukkit.inventory.ItemStack}.
     * @return An {@link net.minecraft.item.ItemStack} clone of the Bukkit stack.
     * @throws UnsupportedOperationException if Bukkit is missing.
     */
    @NotNull ItemStack toForgeStack(@NotNull Object bukkitStack);

    /**
     * Copies and converts a Forge stack into a Bukkit stack.
     *
     * @param minecraftStack An {@link net.minecraft.item.ItemStack}.
     * @return An {@link org.bukkit.inventory.ItemStack} clone of the Forge stack.
     * @throws UnsupportedOperationException if Bukkit is missing.
     */
    @NotNull Object toBukkitStack(@NotNull ItemStack minecraftStack);

    /**
     * Converts an NBTTagCompound to a String representation.
     * It will attempt to use Crucible SNBT if enabled and available.
     *
     * @param tag The NBTTagCompound to convert.
     * @return A string representation of the NBTTagCompound.
     */
    default @NotNull String NBTTagToSNBT(@NotNull NBTTagCompound tag) {
        return tag.toString();
    }


    /**
     * Converts a String representation of a NBTTagCompound to a NBTTagCompound object.
     * It will attempt to use Crucible SNBT if enabled and available.
     *
     * @param tag The String representation of the NBTTagCompound.
     * @return A NBTTagCompound parsed from the string.
     * @throws NBTException If the tag is not a valid compound tag.
     */
    default @NotNull NBTTagCompound NBTTagFromSNBT(@NotNull String tag) throws NBTException {
        var nbt = JsonToNBT.func_150315_a(tag);
        if (nbt instanceof NBTTagCompound) {
            return (NBTTagCompound) nbt;
        } else {
            throw new NBTException("Not a compound tag: " + nbt.getClass());
        }
    }
}

class Provider {
    static SafeBukkit get() {
        return PraesidiumEvolutionis.bukkit ? new WithBukkit() : new WithoutBukkit();
    }
}

final class WithoutBukkit implements SafeBukkit {
    @Override
    public boolean hasBukkit() {
        return false;
    }

    @Override
    public boolean hasCrucible() {
        return false;
    }

    @Override
    public boolean isBukkitStack(@NotNull Object stack) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull EntityPlayerMP player, @NotNull String node) {
        return hasPermission(player, node, 4);
    }

    @Override
    public boolean hasPermission(@NotNull EntityPlayerMP player, @NotNull String node, int fallbackOpLevel) {
        if (fallbackOpLevel <= 0) {
            // Assume permission level 0 is everyone is allowed to run it.
            return true;
        } else if (player.mcServer.getConfigurationManager().canSendCommands(player.getGameProfile())) {
            UserListOpsEntry userlistopsentry = (UserListOpsEntry)player.mcServer.getConfigurationManager().getOppedPlayers().getEntry(player.getGameProfile());
            return player.mcServer.isSinglePlayer() || userlistopsentry != null ? userlistopsentry.func_152644_a() >= fallbackOpLevel : player.mcServer.getOpPermissionLevel() >= fallbackOpLevel;
        } else {
            return false;
        }
    }

    @Override
    public int getPermissionInteger(@NotNull EntityPlayerMP player, @NotNull String node, int defaultValue) {
        return defaultValue;
    }

    @Override
    public @NotNull String getPermissionString(@NotNull EntityPlayerMP player, @NotNull String node, @NotNull String defaultValue) {
        return defaultValue;
    }

    @Override
    public @NotNull ItemStack toForgeStack(@NotNull Object bukkitStack) {
        throw new UnsupportedOperationException("Bukkit is needed for this operation");
    }

    @Override
    public @NotNull Object toBukkitStack(@NotNull ItemStack minecraftStack) {
        throw new UnsupportedOperationException("Bukkit is needed for this operation");
    }
}

@SuppressWarnings("DataFlowIssue")
final class WithBukkit implements SafeBukkit {
    private final boolean crucible = ReflectionHelper.doesClassExist("io.github.crucible.CrucibleModContainer");
    private final boolean crucibleApi = ReflectionHelper.doesClassExist("io.github.crucible.api.CrucibleAPI");
    private final ReflectionHelper.MethodInvoker getBukkitEntity = ReflectionHelper.getMethod(Entity.class, "getBukkitEntity");

    @Override
    public boolean hasBukkit() {
        return true;
    }

    @Override
    public boolean hasCrucible() {
        return crucible;
    }

    @Override
    public boolean isBukkitStack(@NotNull Object stack) {
        return stack instanceof org.bukkit.inventory.ItemStack;
    }

    @Override
    public boolean hasPermission(@NotNull EntityPlayerMP player, @NotNull String node) {
        var bukkitPlayer = (Player) getBukkitEntity.invoke(player);
        return bukkitPlayer.hasPermission(node);
    }

    @Override
    public boolean hasPermission(@NotNull EntityPlayerMP player, @NotNull String node, int fallbackOpLevel) {
        return hasPermission(player, node);
    }

    @Override
    public int getPermissionInteger(@NotNull EntityPlayerMP player, @NotNull String node, int defaultValue) {
        var bukkitPlayer = (Player) getBukkitEntity.invoke(player);
        var nodeValue = defaultValue;
        var len = node.split("\\.").length;
        for (PermissionAttachmentInfo perm : bukkitPlayer.getEffectivePermissions()) {
            String permissionNode = perm.getPermission();
            if (permissionNode.startsWith(node + ".")) {
                try {
                    int foundInt = Integer.parseInt(permissionNode.split("\\.")[len]);
                    if (foundInt > nodeValue) {
                        nodeValue = foundInt;
                    }
                } catch (NumberFormatException ignored) {
                    // Not a valid int node
                }
            }
        }
        return nodeValue;
    }

    @Override
    public @NotNull String getPermissionString(@NotNull EntityPlayerMP player, @NotNull String node, @NotNull String defaultValue) {
        var bukkitPlayer = (Player) getBukkitEntity.invoke(player);
        var len = node.split("\\.").length;
        for (PermissionAttachmentInfo perm : bukkitPlayer.getEffectivePermissions()) {
            String permissionNode = perm.getPermission();
            if (permissionNode.startsWith(node + ".")) {
                return permissionNode.split("\\.")[len];
            }
        }
        return defaultValue;
    }

    @Override
    public @NotNull ItemStack toForgeStack(@NotNull Object bukkitStack) {
        return CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) bukkitStack);
    }

    @Override
    public @NotNull Object toBukkitStack(@NotNull ItemStack minecraftStack) {
        return CraftItemStack.asBukkitCopy(minecraftStack);
    }

    @Override
    public @NotNull String NBTTagToSNBT(@NotNull NBTTagCompound tag) {
        if (crucibleApi && ServerConfig.integration_useModernSNBT) {
            return CrucibleAPI.NBTTagToSNBT(tag);
        } else {
            return SafeBukkit.super.NBTTagToSNBT(tag);
        }
    }

    @Override
    public @NotNull NBTTagCompound NBTTagFromSNBT(@NotNull String tag) throws NBTException {
        if (crucibleApi && ServerConfig.integration_useModernSNBT) {
            try {
                return CrucibleAPI.NBTTagFromSNBT(tag);
            } catch (NBTException e) {
                try {
                    // Try to fall back to the vanilla nbt string
                    return SafeBukkit.super.NBTTagFromSNBT(tag);
                } catch (NBTException suppressed) {
                    e.addSuppressed(suppressed);
                    throw e;
                }
            }
        } else {
            return SafeBukkit.super.NBTTagFromSNBT(tag);
        }
    }
}
