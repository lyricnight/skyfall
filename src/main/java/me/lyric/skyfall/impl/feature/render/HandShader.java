package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.utils.interfaces.ducks.IEntityRenderer;
import me.lyric.skyfall.api.utils.render.image.GifImage;
import me.lyric.skyfall.api.utils.render.image.NamedImage;
import me.lyric.skyfall.api.utils.render.image.TextureStore;
import me.lyric.skyfall.api.utils.shader.world.FramebufferShader;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class HandShader extends Feature {
    public BooleanSetting chams = setting("Chams", true);

    public ModeSetting shaderMode = setting("Shader Mode", "Glow", Arrays.asList("Glow", "Gradient"));

    public BooleanSetting blur = setting("Blur", false)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"));

    public FloatSetting radius = setting("Radius", 4.0f, 0.1f, 10.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"));

    public FloatSetting mix = setting("Mix", 0.5f, 0.0f, 1.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"));

    public BooleanSetting image = setting("Image", false)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"))
            .invokeTab("Image");

    public StringSetting imageToUse = setting("Image To Use", "default.png")
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"))
            .invokeTab("Image");

    public FloatSetting imageMix = setting("ImageMix", 0.8f, 0.0f, 1.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"))
            .invokeTab("Image");

    public ColourSetting colour = setting("Colour", Color.BLUE)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"));

    public ActionSetting reload = setting("Reload", "Reload files", Managers.IMAGES::init)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"))
            .invokeTab("Image");

    public BooleanSetting gif = setting("Gif", false)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"))
            .invokeTab("GIF");

    public StringSetting gifToUse = setting("Gif To Use", "default.gif")
            .invokeVisibility(mode -> shaderMode.getValue().equals("Glow"))
            .invokeTab("GIF");

    public ColourSetting gradientColor1 = setting("Gradient Color 1", new Color(255, 0, 0))
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    public ColourSetting gradientColor2 = setting("Gradient Color 2", new Color(255, 0, 255))
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    public ColourSetting gradientColor3 = setting("Gradient Color 3", new Color(128, 0, 255))
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    public ModeSetting gradientColorCount = setting("Color Count", "3", Arrays.asList("2", "3"))
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    public ModeSetting gradientDirection = setting("Gradient Direction", "Vertical",
            Arrays.asList("Horizontal", "Vertical", "Radial", "Diagonal"))
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    // Outline settings (outline uses gradient colors)
    public FloatSetting outlineAlpha = setting("Outline Alpha", 1.0f, 0.0f, 1.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    public FloatSetting outlineThickness = setting("Outline Thickness", 2f, 1f, 10.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    // Fill settings
    public FloatSetting fillAlpha = setting("Fill Alpha", 1.0f, 0.0f, 1.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");

    public FloatSetting gradientMix = setting("Gradient Mix", 0.8f, 0.0f, 1.0f)
            .invokeVisibility(mode -> shaderMode.getValue().equals("Gradient"))
            .invokeTab("Gradient");


    private final ItemShader shader = new ItemShader();
    private final GradientItemShader gradientShader = new GradientItemShader();
    /**
     * mixin sync variable
     */
    public static boolean forceRender = false;

    private final Map<String, ItemBounds> boundsCache = new HashMap<>();
    private static final int SWING_BUCKETS = 8;
    private static final int BLOCK_BUCKETS = 3;

    private static class ItemBounds {
        float minU, minV, maxU, maxV;

        ItemBounds(float minU, float minV, float maxU, float maxV) {
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
        }
    }

    public HandShader() {
        super("HandShader", Category.Render);
    }

    public void clearBoundsCache() {
        boundsCache.clear();
    }

    /**
     * gens a cache key based on item and its animation state
     */
    private String getCacheKey(ItemStack itemStack, float swingProgress, boolean isBlocking, int itemInUseCount) {
        if (itemStack == null) return "null";
        Item item = itemStack.getItem();
        int swingBucket = (int) (swingProgress * SWING_BUCKETS);
        String blockingState = "idle";
        if (isBlocking && itemInUseCount > 0) {
            int blockProgress = Math.min(itemInUseCount, 10);
            int blockBucket = (blockProgress * BLOCK_BUCKETS) / 10;
            blockingState = "block_" + blockBucket;
        }

        return item.getUnlocalizedName() + "_s" + swingBucket + "_" + blockingState;
    }

    /**
     * Get or calculate item bounds with intelligent caching.
     * Caches bounds for different animation states to maintain performance
     * while adapting to swing animations and blocking.
     */
    private void getOrCalculateItemBounds(GradientItemShader shader, ItemStack heldItem, Framebuffer framebuffer, float swingProgress, boolean isBlocking, int itemInUseCount) {
        if (heldItem == null) {
            shader.itemMinU = 0.0f;
            shader.itemMinV = 0.0f;
            shader.itemMaxU = 1.0f;
            shader.itemMaxV = 1.0f;
            return;
        }
        String cacheKey = getCacheKey(heldItem, swingProgress, isBlocking, itemInUseCount);
        if (boundsCache.containsKey(cacheKey)) {
            ItemBounds bounds = boundsCache.get(cacheKey);
            shader.itemMinU = bounds.minU;
            shader.itemMinV = bounds.minV;
            shader.itemMaxU = bounds.maxU;
            shader.itemMaxV = bounds.maxV;
        } else {
            calculateItemBoundsOptimized(shader, framebuffer);
            ItemBounds bounds = new ItemBounds(
                    shader.itemMinU,
                    shader.itemMinV,
                    shader.itemMaxU,
                    shader.itemMaxV
            );
            boundsCache.put(cacheKey, bounds);
            if (boundsCache.size() > 10000) {
                boundsCache.clear();
            }
        }
    }

    /**
     * Calculate item bounds from framebuffer using optimized sampling.
     * This is called ONLY when an item at a specific animation state is first rendered.
     */
    private void calculateItemBoundsOptimized(GradientItemShader shader, Framebuffer framebuffer) {
        int width = mc.displayWidth;
        int height = mc.displayHeight;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        int minX = width, minY = height;
        int maxX = 0, maxY = 0;
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                int index = (y * width + x) * 4 + 3; // Alpha channel
                int alpha = buffer.get(index) & 0xFF;

                if (alpha > 0) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX >= minX && maxY >= minY) {
            shader.itemMinU = (float) minX / width;
            shader.itemMinV = (float) minY / height;
            shader.itemMaxU = (float) (maxX + 4) / width;
            shader.itemMaxV = (float) (maxY + 4) / height;
        } else {
            shader.itemMinU = 0.0f;
            shader.itemMinV = 0.0f;
            shader.itemMaxU = 1.0f;
            shader.itemMaxV = 1.0f;
        }
    }


    public void onRenderWorld() {
        if (Display.isActive() || Display.isVisible())
        {
            if (chams.getValue())
            {
                GlStateManager.pushMatrix();
                GlStateManager.pushAttrib();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableAlpha();

                if (shaderMode.getValue().equals("Gradient")) {
                    gradientShader.color1 = gradientColor1.getValue();
                    gradientShader.color2 = gradientColor2.getValue();
                    gradientShader.color3 = gradientColor3.getValue();
                    gradientShader.colorCount = gradientColorCount.getValue().equals("3") ? 3 : 2;

                    String dir = gradientDirection.getValue();
                    switch (dir) {
                        case "Horizontal":
                            gradientShader.gradientDirection = 0;
                            break;
                        case "Vertical":
                            gradientShader.gradientDirection = 1;
                            break;
                        case "Radial":
                            gradientShader.gradientDirection = 2;
                            break;
                        case "Diagonal":
                            gradientShader.gradientDirection = 3;
                            break;
                    }

                    gradientShader.outlineAlpha = outlineAlpha.getValue();
                    gradientShader.outlineThickness = outlineThickness.getValue();

                    gradientShader.fillAlpha = fillAlpha.getValue();
                    gradientShader.mixFactor = gradientMix.getValue();

                    gradientShader.startDraw(mc.timer.renderPartialTicks);
                    forceRender = true;
                    ((IEntityRenderer) mc.entityRenderer).skyfall$invokeRenderHand(mc.timer.renderPartialTicks, 2);

                    ItemStack heldItem = mc.thePlayer.getHeldItem();
                    float swingProgress = mc.thePlayer.getSwingProgress(mc.timer.renderPartialTicks);
                    int itemInUseCount = mc.thePlayer.getItemInUseCount();
                    boolean isBlocking = heldItem != null && mc.thePlayer.isBlocking();
                    getOrCalculateItemBounds(gradientShader, heldItem, gradientShader.getFramebuffer(), swingProgress, isBlocking, itemInUseCount);
                    forceRender = false;
                    gradientShader.stopDraw(Color.WHITE, 1.0f, 1.0f);
                }
                else {
                    Color shaderColor = colour.getValue();
                    shader.blur = blur.getValue();
                    shader.mix = mix.getValue();
                    shader.alpha = shaderColor.getAlpha() / 255.0f;
                    shader.imageMix = imageMix.getValue();
                    shader.useImage = image.getValue();
                    shader.startDraw(mc.timer.renderPartialTicks);
                    forceRender = true;
                    ((IEntityRenderer) mc.entityRenderer).skyfall$invokeRenderHand(mc.timer.renderPartialTicks, 2);
                    forceRender = false;
                    shader.stopDraw(shaderColor, radius.getValue(), 1.0f);
                }
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.disableDepth();
                GlStateManager.popAttrib();
                GlStateManager.popMatrix();
            }
        }
    }



    private class ItemShader extends FramebufferShader {
        public boolean blur;
        public float mix = 0.0f;
        public float alpha = 1.0f;
        public float imageMix = 0.0f;
        public boolean useImage;

        public ItemShader() {
            super("itemglow.frag");
        }

        @Override
        public void setupUniforms()
        {
            setupUniform("texture");
            setupUniform("texelSize");
            setupUniform("color");
            setupUniform("divider");
            setupUniform("radius");
            setupUniform("maxSample");
            setupUniform("dimensions");
            setupUniform("blur");
            setupUniform("mixFactor");
            setupUniform("minAlpha");
            setupUniform("image");
            setupUniform("imageMix");
            setupUniform("useImage");
        }

        @Override
        public void updateUniforms()
        {
            GL20.glUniform1i(getUniform("texture"), 0);
            GL20.glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth * (radius * quality), 1F / mc.displayHeight * (radius * quality));
            GL20.glUniform3f(getUniform("color"), red, green, blue);
            GL20.glUniform1f(getUniform("divider"), 140F);
            GL20.glUniform1f(getUniform("radius"), radius);
            GL20.glUniform1f(getUniform("maxSample"), 10F);
            GL20.glUniform2f(getUniform("dimensions"), mc.displayWidth, mc.displayHeight);
            GL20.glUniform1i(getUniform("blur"), blur ? 1 : 0);
            GL20.glUniform1f(getUniform("mixFactor"), mix);
            GL20.glUniform1f(getUniform("minAlpha"), alpha);
            GL13.glActiveTexture(GL13.GL_TEXTURE8);
            if (gif.getValue())
            {
                GifImage image = Managers.IMAGES.getGifByName(gifToUse.getValue());
                if (image == null) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                }
                else {
                    TextureStore texture = Managers.IMAGES.getGifByName(gifToUse.getValue()).getDynamicTexture();
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture != null ? texture.getGlTextureId() : 0);
                }
            }
            else
            {
                NamedImage image2 = Managers.IMAGES.getImageByName(imageToUse.getValue());
                if (image2 == null) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                }
                else {
                    TextureStore texture = Managers.IMAGES.getImageByName(imageToUse.getValue()).getTextureStore();
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture != null ? texture.getGlTextureId() : 0);
                }
            }
            GL20.glUniform1i(getUniform("image"), 8);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL20.glUniform1f(getUniform("imageMix"), imageMix);
            GL20.glUniform1i(getUniform("useImage"), useImage ? 1 : 0);
        }
    }

    private static class GradientItemShader extends FramebufferShader {
        public Color color1 = Color.RED;
        public Color color2 = Color.MAGENTA;
        public Color color3 = Color.BLUE;
        public int colorCount = 3;
        public int gradientDirection = 1; // 0=horizontal, 1=vertical, 2=radial, 3=diagonal (default vertical)

        public float outlineAlpha = 1.0f;
        public float outlineThickness = 1.5f;

        public float fillAlpha = 1.0f;
        public float mixFactor = 0.8f;

        public float itemMinU = 0.0f;
        public float itemMinV = 0.0f;
        public float itemMaxU = 1.0f;
        public float itemMaxV = 1.0f;

        public GradientItemShader() {
            super("itemgradient.frag");
        }

        @Override
        public void setupUniforms() {
            setupUniform("texture");
            setupUniform("texelSize");
            setupUniform("dimensions");
            setupUniform("color1");
            setupUniform("color2");
            setupUniform("color3");
            setupUniform("colorCount");
            setupUniform("gradientDirection");
            setupUniform("outlineAlpha");
            setupUniform("outlineThickness");
            setupUniform("fillAlpha");
            setupUniform("mixFactor");
            setupUniform("itemMinU");
            setupUniform("itemMinV");
            setupUniform("itemMaxU");
            setupUniform("itemMaxV");
        }

        @Override
        public void updateUniforms() {
            GL20.glUniform1i(getUniform("texture"), 0);
            GL20.glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth, 1F / mc.displayHeight);
            GL20.glUniform2f(getUniform("dimensions"), mc.displayWidth, mc.displayHeight);
            GL20.glUniform3f(getUniform("color1"),
                    color1.getRed() / 255f,
                    color1.getGreen() / 255f,
                    color1.getBlue() / 255f);
            GL20.glUniform3f(getUniform("color2"),
                    color2.getRed() / 255f,
                    color2.getGreen() / 255f,
                    color2.getBlue() / 255f);
            GL20.glUniform3f(getUniform("color3"),
                    color3.getRed() / 255f,
                    color3.getGreen() / 255f,
                    color3.getBlue() / 255f);
            GL20.glUniform1i(getUniform("colorCount"), colorCount);
            GL20.glUniform1i(getUniform("gradientDirection"), gradientDirection);
            GL20.glUniform1f(getUniform("outlineAlpha"), outlineAlpha);
            GL20.glUniform1f(getUniform("outlineThickness"), outlineThickness);
            GL20.glUniform1f(getUniform("fillAlpha"), fillAlpha);
            GL20.glUniform1f(getUniform("mixFactor"), mixFactor);
            GL20.glUniform1f(getUniform("itemMinU"), itemMinU);
            GL20.glUniform1f(getUniform("itemMinV"), itemMinV);
            GL20.glUniform1f(getUniform("itemMaxU"), itemMaxU);
            GL20.glUniform1f(getUniform("itemMaxV"), itemMaxV);
        }
    }
}