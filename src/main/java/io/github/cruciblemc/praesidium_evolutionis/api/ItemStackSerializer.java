package io.github.cruciblemc.praesidium_evolutionis.api;

import cpw.mods.fml.common.registry.GameRegistry;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackSerializer {

    /**
     * Encodes an item stack into a string in the following format: <itemRegistry:metadata:amount>(nbt)
     *
     * @param stack - item stack to encode.
     * @return The encoded item stack.
     */
    @NotNull
    public static String encodeItem(ItemStack stack) {
        var identifier = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        if (identifier == null) {
            throw new UnsupportedOperationException("Unable to serialize custom item stack");
        }
        var item = identifier.toString();
        item = '<' + item + ':' + stack.getMetadata() + ':' + stack.stackSize + '>';
        if (stack.stackTagCompound == null) return item;
        else {
            return item + '(' + SafeBukkit.REF.NBTTagToSNBT(stack.stackTagCompound) + ')';
        }
    }

    /**
     * Parses an encoded item stack with the <itemRegistry:metadata:amount>(nbt) format back into an ItemStack.
     *
     * @param serializedItem - the string which contains the serialized item.
     * @return A forge item stack of the item.
     * @throws BadStackFormatException  if it has an invalid stack string.
     * @throws BadNbtException          if it has an invalid nbt.
     * @throws MissingRegistryException if the item it tried to decode does not exist.
     */
    @NotNull
    public static ItemStack parseItem(String serializedItem) throws BadStackFormatException, BadNbtException, MissingRegistryException {
        int fistStackToken, secondStackToken, firstNBTToken, secondNBTToken, metadata, amount;
        String nbt, itemIdentifier, registry;

        //Search for the nbt token.
        firstNBTToken = serializedItem.indexOf('(');
        secondNBTToken = serializedItem.lastIndexOf(')');
        if (firstNBTToken == -1 && secondNBTToken == -1) {
            //No nbt found.
            nbt = "";
        } else if (firstNBTToken != -1 && secondNBTToken != -1) {
            //Extract the nbt string.
            nbt = serializedItem.substring(firstNBTToken + 1, secondNBTToken);
            serializedItem = serializedItem.substring(0, firstNBTToken);
        } else {
            //Unbalanced nbt token, either corrupted or badly formatted.
            throw new BadStackFormatException("Invalid nbt identifier", serializedItem);
        }

        //Search for the item stack token.
        fistStackToken = serializedItem.indexOf('<');
        secondStackToken = serializedItem.lastIndexOf('>');
        if (fistStackToken == -1 || secondStackToken == -1) {
            //Unbalanced stack token, either corrupted or badly formatted.
            throw new BadStackFormatException("Invalid stack identifier", serializedItem);
        }

        //Extract all necessary information to reconstruct the stack.
        itemIdentifier = serializedItem.substring(fistStackToken + 1, secondStackToken);
        String[] splitIdentifier = itemIdentifier.split(":");
        if (splitIdentifier.length != 4) {
            //Missing or additional information, consider it invalid.
            throw new BadStackFormatException("Invalid stack identifier", serializedItem);
        }

        //Reconstruct the item registry and the stack attributes.
        registry = splitIdentifier[0] + ':' + splitIdentifier[1];
        try {
            metadata = Integer.parseInt(splitIdentifier[2]);
            amount = Integer.parseInt(splitIdentifier[3]);
            if (amount <= 0 || amount > 64)
                throw new NumberFormatException("Invalid stack size: " + amount);
        } catch (NumberFormatException e) {
            throw new BadStackFormatException("Metadata or amount is not a number:", serializedItem, e);
        }

        //Search for the item in the item registry.
        Item item = (Item) Item.itemRegistry.getObject(registry);
        if (item == null) {
            throw new MissingRegistryException(registry);
        }

        //Create a new item stack with all the parsed information.
        ItemStack itemStack = new ItemStack(item, amount, metadata);
        if (!nbt.isEmpty()) {
            try {
                itemStack.setTagCompound(SafeBukkit.REF.NBTTagFromSNBT(nbt));
            } catch (NBTException e) {
                throw new BadNbtException(nbt, e);
            }
        }

        return itemStack;
    }

    /**
     * Same as {@link #parseItem(String)} but will return null if an exception arises while decoding the item.
     *
     * @param serializedItem - the string which contains the serialized item
     * @return A forge item stack of the item.
     */
    @Nullable
    public static ItemStack parseItemOrNull(String serializedItem) {
        try {
            return parseItem(serializedItem);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * This exception is thrown when an invalid nbt is parsed
     */
    public static class BadNbtException extends Exception {
        /**
         * The nbt which caused the exception
         */
        @Getter
        private final String nbt;

        BadNbtException(String nbt, Throwable parent) {
            super("Invalid nbt tag: " + nbt, parent);
            this.nbt = nbt;
        }
    }

    /**
     * This exception is thrown when an item is missing in the registry
     */
    public static class MissingRegistryException extends Exception {
        /**
         * The item registry which caused the exception
         */
        @Getter
        private final String registryComponent;

        MissingRegistryException(String registryComponent) {
            super("No such item:" + registryComponent);
            this.registryComponent = registryComponent;
        }
    }

    /**
     * This exception is thrown when an invalid item stack is parsed
     */
    public static class BadStackFormatException extends Exception {
        /**
         * The item stack which caused the exception
         */
        @Getter
        private final String badStack;

        BadStackFormatException(String reason, String stack) {
            super(reason + stack);
            this.badStack = stack;
        }

        BadStackFormatException(String reason, String stack, Throwable cause) {
            super(reason + stack, cause);
            this.badStack = stack;
        }
    }
}
