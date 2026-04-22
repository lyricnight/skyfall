package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.ColourSetting;
import me.lyric.skyfall.api.setting.types.FloatSetting;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.api.utils.player.MovementUtils;
import me.lyric.skyfall.api.utils.render.FeatureRenderUtils;
import me.lyric.skyfall.api.utils.shader.GradientShader;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;

/**
 * waiter! waiter! more render slop please!
 */
public final class Crosshair extends Feature {

    public ModeSetting gap = setting("Gap", "Normal", Arrays.asList("Normal", "None", "Normal", "Dynamic")).invokeTab("Structure");

    public ModeSetting colorMode = setting("Color Mode", "Static", Arrays.asList("Static", "Gradient")).invokeTab("Colour");

    public ColourSetting colour = setting("Colour", Color.BLACK).invokeTab("Colour").invokeVisibility(v -> colorMode.getValue().equals("Static"));

    public ColourSetting outlineColour = setting("Outline Colour", Color.WHITE).invokeTab("Colour").invokeVisibility(v -> colorMode.getValue().equals("Static"));

    public ColourSetting gradientColor1 = setting("Primary", new Color(113, 93, 214)).invokeTab("Colour").invokeVisibility(v -> colorMode.getValue().equals("Gradient"));

    public ColourSetting gradientColor2 = setting("Secondary", new Color(113, 220, 214)).invokeTab("Colour").invokeVisibility(v -> colorMode.getValue().equals("Gradient"));

    public FloatSetting gradientSpeed = setting("Speed", 6.0f, 0.1f, 15f).invokeTab("Colour").invokeVisibility(v -> colorMode.getValue().equals("Gradient"));

    public FloatSetting gradientStep = setting("Step", 0.3f, 0.1f, 1.0f).invokeTab("Colour").invokeVisibility(v -> colorMode.getValue().equals("Gradient"));

    public FloatSetting length = setting("Length", 4.0f, 0.5f, 30.0f).invokeTab("Structure");

    public FloatSetting width = setting("Width", 1.0f, 0.1f, 10.0f).invokeTab("Structure");

    public FloatSetting gapSize = setting("Gap Size", 2.0f, 0f, 20f).invokeTab("Structure").invokeVisibility(v -> !gap.getValue().equals("None"));

    public Crosshair() {
        super("Crosshair", Category.Render);
    }

    public static boolean flag = false;

    @Override
    public void onDisable()
    {
        GuiIngameForge.renderCrosshairs = true;
    }

    @Override
    public void onRender2D()
    {
        if (mc.currentScreen != null || mc.gameSettings.thirdPersonView != 0) return;
        GuiIngameForge.renderCrosshairs = false;
        ScaledResolution sr = new ScaledResolution(mc);
        final int middleX = sr.getScaledWidth() / 2;
        final int middleY = sr.getScaledHeight() / 2;
        final float widthUsed = width.getValue();
        renderWithShader(() -> {
            if (!gap.getValue().equals("None"))
            {
                //top
                FeatureRenderUtils.drawBorderedRect(middleX - widthUsed, middleY - (gapSize.getValue() + length.getValue()) - ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue() : 0), middleX + (width.getValue()), middleY - (gapSize.getValue()) - ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue() : 0), 0.5f, getColorRGB(), getOutlineColorRGB());
                //bottom
                FeatureRenderUtils.drawBorderedRect(middleX - widthUsed, middleY + (gapSize.getValue()) + ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue() : 0), middleX + (width.getValue()), middleY + (gapSize.getValue() + length.getValue()) + ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue() : 0), 0.5f, getColorRGB(), getOutlineColorRGB());
                //left
                FeatureRenderUtils.drawBorderedRect(middleX - (gapSize.getValue() + length.getValue()) - ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue(): 0), middleY - (width.getValue()), middleX - (gapSize.getValue()) - ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue(): 0), middleY + (width.getValue()), 0.5f, getColorRGB(), getOutlineColorRGB());
                //right
                FeatureRenderUtils.drawBorderedRect(middleX + (gapSize.getValue()) + ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue(): 0), middleY - (width.getValue()), middleX + (gapSize.getValue() + length.getValue()) + ((MovementUtils.isMoving() && gap.getValue().equals("Dynamic")) ? gapSize.getValue(): 0), middleY + (width.getValue()), 0.5f, getColorRGB(), getOutlineColorRGB());
            }
        });

        GuiIngameForge.renderCrosshairs = false;
    }

    private void renderWithShader(Runnable renderCallback) {
        if (colorMode.getValue().equals("Gradient")) {
            GradientShader.setup(gradientStep.getValue(), gradientSpeed.getValue(), gradientColor1.getValue(), gradientColor2.getValue());
            try {
                renderCallback.run();
            } finally {
                GradientShader.finish();
                GL11.glLineWidth(1.0f);
            }
        } else {
            renderCallback.run();
        }
    }

    private int getColorRGB() {
        return colorMode.getValue().equals("Gradient") ? Color.WHITE.getRGB() : colour.getValue().getRGB();
    }

    private int getOutlineColorRGB() {
        return colorMode.getValue().equals("Gradient") ? Color.WHITE.getRGB() : outlineColour.getValue().getRGB();
    }
}
