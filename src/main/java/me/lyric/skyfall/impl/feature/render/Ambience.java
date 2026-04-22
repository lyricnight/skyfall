package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.ActionSetting;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.ColourSetting;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.ducks.IEntityRenderer;
import me.lyric.skyfall.api.utils.nulls.Null;

import java.awt.*;
import java.lang.reflect.Field;

public final class Ambience extends Feature {

    public ColourSetting colour = setting("Colour", new Color(99, 70, 230, 100)).invokeTab("Colour");

    public ActionSetting apply = setting("Click", "Manual Reloading", this::reloadRenderers).invokeTab("Rendering");

    public BooleanSetting force = setting("Force Reload", false).invokeTab("Rendering");

    private boolean lightPipeline;

    /**
     * Cached color to avoid allocation every frame
     */
    private Color cachedColor = null;
    private int lastR = -1, lastG = -1, lastB = -1, lastA = -1;

    public Ambience() {
        super("Ambience", Category.Render);
    }

    @Override
    public void onInit()
    {
        try
        {
            Field field = Class.forName("net.minecraftforge.common.ForgeModContainer", true, this.getClass().getClassLoader()).getDeclaredField("forgeLightPipelineEnabled");
            boolean accessible = field.isAccessible();
            Skyfall.LOGGER.info("Accessing forgeLightPipelineEnabled field, accessible: {}", accessible);
            field.setAccessible(true);
            lightPipeline = field.getBoolean(null);
            field.setAccessible(accessible);
        }
        catch (Exception e)
        {
            //this will catch everytime forge is not present, or it's supposed to ig?
            ExceptionHandler.handle(e, this.getClass());
        }
    }

    @Override
    public void onEnable()
    {
        try
        {
            Field field = Class.forName("net.minecraftforge.common.ForgeModContainer", true, this.getClass().getClassLoader()).getDeclaredField("forgeLightPipelineEnabled");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            lightPipeline = field.getBoolean(null);
            field.set(null, false);
            field.setAccessible(accessible);
        }
        catch (Exception e)
        {
            //this will catch everytime forge is not present.
            ExceptionHandler.handle(e, this.getClass());
        }
        reloadRenderers();
        ((IEntityRenderer)mc.entityRenderer).lightMapUpdate(true);
    }

    @Override
    public void onDisable()
    {
        try
        {
            Field field = Class.forName("net.minecraftforge.common.ForgeModContainer", true, this.getClass().getClassLoader()).getDeclaredField("forgeLightPipelineEnabled");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(null, lightPipeline);
            field.setAccessible(accessible);
        }
        catch (Exception e)
        {
            //this will catch everytime forge is not present.
            ExceptionHandler.handle(e, this.getClass());
        }
        reloadRenderers();
    }

    @Override
    public String displayAppend()
    {
        return " " +  colour.getValue().getRed() + ", " + colour.getValue().getGreen() + ", " + colour.getValue().getBlue();
    }


    public Color getColor()
    {
        Color c = colour.getValue();
        if (cachedColor == null || c.getRed() != lastR || c.getGreen() != lastG || c.getBlue() != lastB || c.getAlpha() != lastA) {
            lastR = c.getRed();
            lastG = c.getGreen();
            lastB = c.getBlue();
            lastA = c.getAlpha();
            cachedColor = new Color(lastR, lastG, lastB, lastA);
        }
        return cachedColor;
    }

    private void reloadRenderers()
    {
        if (Null.is() || mc.renderGlobal == null || mc.gameSettings == null) return;
        Skyfall.LOGGER.info("Reloading Renderers called by Ambience");
        if (force.getValue())
        {
            mc.renderGlobal.loadRenderers();
            Skyfall.LOGGER.info("Reloading Renderers forced.");
            return;
        }
        int x = (int) mc.thePlayer.posX;
        int y = (int) mc.thePlayer.posY;
        int z = (int) mc.thePlayer.posZ;
        int d = mc.gameSettings.renderDistanceChunks * 16;
        mc.renderGlobal.markBlockRangeForRenderUpdate(x - d, y - d, z - d, x + d, y + d, z + d);
    }
}
