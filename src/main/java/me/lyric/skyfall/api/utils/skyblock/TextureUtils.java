package me.lyric.skyfall.api.utils.skyblock;

import lombok.experimental.UtilityClass;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

@UtilityClass
public class TextureUtils {
    public String getSkullTexture(ItemStack item) {
        if (item == null) {
            return null;
        }

        NBTTagCompound compound = item.getTagCompound();
        if (compound == null || !compound.hasKey("SkullOwner")) {
            return null;
        }

        NBTTagCompound skullOwner = compound.getCompoundTag("SkullOwner");
        if (!skullOwner.hasKey("Properties")) {
            return null;
        }

        NBTTagCompound properties = skullOwner.getCompoundTag("Properties");
        if (!properties.hasKey("textures")) {
            return null;
        }

        NBTTagList texturesList = properties.getTagList("textures", 10);
        if (texturesList.tagCount() == 0) {
            return null;
        }

        NBTTagCompound firstTexture = texturesList.getCompoundTagAt(0);
        return firstTexture.hasKey("Value") ? firstTexture.getString("Value") : null;
    }
}
