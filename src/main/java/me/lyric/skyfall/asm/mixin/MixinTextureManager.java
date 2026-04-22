package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.utils.interfaces.ducks.ITextureManager;
import me.lyric.skyfall.api.utils.render.image.TextureStore;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager implements ITextureManager
{
    @Shadow
    @Final
    private Map<String, Integer> mapTextureCounters;

    @Shadow public abstract boolean loadTexture(ResourceLocation textureLocation, ITextureObject textureObj);

    @Unique
    @Override
    public ResourceLocation skyfall$getTextureResourceLocation(String name, TextureStore texture)
    {
        Integer integer = mapTextureCounters.get(name);

        if (integer == null)
        {
            integer = 1;
        }
        else
        {
            integer = integer + 1;
        }
        this.mapTextureCounters.put(name, integer);
        ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", name, integer));
        loadTexture(resourcelocation, texture);
        return resourcelocation;
    }
}