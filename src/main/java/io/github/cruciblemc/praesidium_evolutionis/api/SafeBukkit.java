package io.github.cruciblemc.praesidium_evolutionis.api;

import io.github.crucible.api.CrucibleAPI;
import io.github.cruciblemc.praesidium_evolutionis.PraesidiumEvolutionis;
import io.github.cruciblemc.praesidium_evolutionis.config.ServerConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.apiguardian.api.API;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
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
    boolean isBukkitStack(Object stack);

    /**
     * Copies and converts a Bukkit stack to a Minecraft stack.
     *
     * @param bukkitStack An {@link org.bukkit.inventory.ItemStack}.
     * @return An {@link net.minecraft.item.ItemStack} clone of the Bukkit stack.
     * @throws UnsupportedOperationException if Bukkit is missing.
     */
    @NotNull ItemStack toForgeStack(Object bukkitStack);

    /**
     * Copies and converts a Forge stack into a Bukkit stack.
     *
     * @param minecraftStack An {@link net.minecraft.item.ItemStack}.
     * @return An {@link org.bukkit.inventory.ItemStack} clone of the Forge stack.
     * @throws UnsupportedOperationException if Bukkit is missing.
     */
    @NotNull Object toBukkitStack(ItemStack minecraftStack);

    /**
     * Converts an NBTTagCompound to a String representation.
     * It will attempt to use Crucible SNBT if enabled and available.
     *
     * @param tag The NBTTagCompound to convert.
     * @return A string representation of the NBTTagCompound.
     */
    default @NotNull String NBTTagToSNBT(NBTTagCompound tag) {
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
    default @NotNull NBTTagCompound NBTTagFromSNBT(String tag) throws NBTException {
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
    public boolean isBukkitStack(Object stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack toForgeStack(Object bukkitStack) {
        throw new UnsupportedOperationException("Bukkit is needed for this operation");
    }

    @Override
    public @NotNull Object toBukkitStack(ItemStack minecraftStack) {
        throw new UnsupportedOperationException("Bukkit is needed for this operation");
    }
}

@SuppressWarnings("DataFlowIssue")
final class WithBukkit implements SafeBukkit {
    private final boolean crucible = ReflectionHelper.doesClassExist("io.github.crucible.CrucibleModContainer");
    private final boolean crucibleApi = ReflectionHelper.doesClassExist("io.github.crucible.api.CrucibleAPI");

    @Override
    public boolean hasBukkit() {
        return true;
    }

    @Override
    public boolean hasCrucible() {
        return crucible;
    }

    @Override
    public boolean isBukkitStack(Object stack) {
        return stack instanceof org.bukkit.inventory.ItemStack;
    }

    @Override
    public @NotNull ItemStack toForgeStack(Object bukkitStack) {
        return CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) bukkitStack);
    }

    @Override
    public @NotNull Object toBukkitStack(ItemStack minecraftStack) {
        return CraftItemStack.asBukkitCopy(minecraftStack);
    }

    @Override
    public @NotNull String NBTTagToSNBT(NBTTagCompound tag) {
        if (crucibleApi && ServerConfig.integration_useModernSNBT) {
            return CrucibleAPI.NBTTagToSNBT(tag);
        } else {
            return SafeBukkit.super.NBTTagToSNBT(tag);
        }
    }

    @Override
    public @NotNull NBTTagCompound NBTTagFromSNBT(String tag) throws NBTException {
        if (crucibleApi && ServerConfig.integration_useModernSNBT) {
            try {
                return CrucibleAPI.NBTTagFromSNBT(tag);
            } catch (NBTException e) {
                try {
                    // Try to fallback to the vanilla nbt string
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
