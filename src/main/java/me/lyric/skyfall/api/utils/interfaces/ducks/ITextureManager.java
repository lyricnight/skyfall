package me.lyric.skyfall.api.utils.interfaces.ducks;

import me.lyric.skyfall.api.utils.render.image.TextureStore;
import net.minecraft.util.ResourceLocation;

public interface ITextureManager {
    ResourceLocation skyfall$getTextureResourceLocation(String name, TextureStore textureStore);
}
