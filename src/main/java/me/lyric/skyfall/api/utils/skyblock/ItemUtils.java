package me.lyric.skyfall.api.utils.skyblock;

import lombok.experimental.UtilityClass;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@UtilityClass
public class ItemUtils {

    public static String skyblockID(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return "";
        NBTTagCompound extraAttributes = stack.getTagCompound().getCompoundTag("ExtraAttributes");
        return extraAttributes != null ? extraAttributes.getString("id") : "";
    }

    /**
     * Gets the ExtraAttributes NBT compound from an ItemStack
     * @param stack The ItemStack to get ExtraAttributes from
     * @return The ExtraAttributes compound
     */
    public static NBTTagCompound getExtraAttributes(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return null;
        return stack.getTagCompound().getCompoundTag("ExtraAttributes");
    }

    /**
     * checks if the item has etherwarp
     * @param stack The ItemStack to check
     * @return what u fucking think
     */
    public static boolean hasEthermerge(ItemStack stack) {
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        return extraAttributes != null && extraAttributes.getBoolean("ethermerge");
    }

    /**
     * Gets the tuned transmission bonus
     * @param stack The ItemStack to check
     * @return The tuner bonus, or 0 if none
     */
    public static int getTunerBonus(ItemStack stack) {
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        return extraAttributes != null ? extraAttributes.getInteger("tuned_transmission") : 0;
    }

    /**
     * player is currently using etherwarp
     * @param player The player to check
     * @return true if the player is using etherwarp
     */
    public static boolean isUsingEtherwarp(EntityPlayerSP player) {
        if (player == null || player.getHeldItem() == null) return false;
        String skyblockID = skyblockID(player.getHeldItem());
        if ("ETHERWARP_CONDUIT".equals(skyblockID)) return true;
        return player.isSneaking() && hasEthermerge(player.getHeldItem());
    }
}
