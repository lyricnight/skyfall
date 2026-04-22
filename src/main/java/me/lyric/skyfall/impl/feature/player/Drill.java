package me.lyric.skyfall.impl.feature.player;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.nulls.Null;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

/**
 * @author various people
 */
public final class Drill extends Feature {
    public Drill()
    {
        super("Drill",Category.Player);
    }

    public boolean isHittingPosition(BlockPos pos, ItemStack currentItemHittingBlock, BlockPos currentBlock)
    {
        ItemStack itemstack = mc.thePlayer.getHeldItem();
        boolean flag = currentItemHittingBlock == null && itemstack == null;

        if (currentItemHittingBlock != null && itemstack != null)
        {
            if (isEnabled() && itemstack.getItem() == currentItemHittingBlock.getItem() && getUUID(itemstack).equals(getUUID(currentItemHittingBlock)) && checkItem(itemstack.getItem())) {
                return pos.equals(currentBlock);
            }

            flag = itemstack.getItem() == currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, currentItemHittingBlock) &&
                    (
                            itemstack.isItemStackDamageable() || itemstack.getMetadata() == currentItemHittingBlock.getMetadata()
                    );
        }
        return pos.equals(currentBlock) && flag;
    }

    public String getUUID(ItemStack item) {
        try {
            NBTTagCompound nbt = item.getSubCompound("ExtraAttributes",false);
            return nbt.getString("uuid");
        } catch (Exception e) {
            return "";
        }
    }

    private boolean checkItem(Item item)
    {
        if (item == null) return false;
        return item == Item.getItemById(409) ||  // prismarine shard
                item == Item.getItemById(397) ||  // player head
                item == Item.getItemById(270) ||  // wooden pickaxe
                item == Item.getItemById(274) ||  // stone pickaxe
                item == Item.getItemById(257) ||  // iron pickaxe
                item == Item.getItemById(285) ||  // golden pickaxe
                item == Item.getItemById(278);
    }

    @Override
    public String displayAppend()
    {
        try {
            if (Null.is() || mc.thePlayer.getHeldItem() == null)
            {
                return " false";
            }
            if (checkItem(mc.thePlayer.getHeldItem().getItem())) {
                return " true";
            }
            else {
                return " false";
            }
        }
        catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        return " ";
    }

}
