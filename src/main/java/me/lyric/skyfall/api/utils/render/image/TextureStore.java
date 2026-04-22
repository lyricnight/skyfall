package me.lyric.skyfall.api.utils.render.image;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import java.awt.image.BufferedImage;

/**
 * @author lyric
 * used to hold image textures.
 */
public class TextureStore extends AbstractTexture {
    private int[] textureData;
    private final int width;
    private final int height;

    public TextureStore(BufferedImage bufferedImage)
    {
        this(bufferedImage.getWidth(), bufferedImage.getHeight());
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.textureData, 0, bufferedImage.getWidth());
        this.updateTextureStore();
    }

    public TextureStore(int textureWidth, int textureHeight)
    {
        this.width = textureWidth;
        this.height = textureHeight;
        this.textureData = new int[textureWidth * textureHeight];
        TextureUtil.allocateTexture(this.getGlTextureId(), textureWidth, textureHeight);
    }

    public void loadTexture(IResourceManager resourceManager)
    {
        //this is only ever going to be used when we want to load this into the resource manager to render inside minecraft's rendering pipeline
    }

    private void updateTextureStore()
    {
        TextureUtil.uploadTexture(this.getGlTextureId(), this.textureData, this.width, this.height);
        textureData = new int[0];
    }
}
