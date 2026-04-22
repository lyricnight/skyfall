package me.lyric.skyfall.api.utils.shader;

import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.ShaderUtils;
import me.lyric.skyfall.api.utils.shader.framework.ShaderStateManager;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class GradientShader implements Globals {
    private final static ShaderUtils shader = new ShaderUtils("gradient");
    private static float lastStep = -1f;
    private static int lastColorR = -1, lastColorG = -1, lastColorB = -1;
    private static int lastColor2R = -1, lastColor2G = -1, lastColor2B = -1;

    private static void setupUniforms(float step, float speed, Color color, Color color2) {
        shader.setUniformi("texture", 0);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        if (r != lastColorR || g != lastColorG || b != lastColorB) {
            shader.setUniformf("rgb", r / 255.0f, g / 255.0f, b / 255.0f);
            lastColorR = r;
            lastColorG = g;
            lastColorB = b;
        }

        int r2 = color2.getRed(), g2 = color2.getGreen(), b2 = color2.getBlue();
        if (r2 != lastColor2R || g2 != lastColor2G || b2 != lastColor2B) {
            shader.setUniformf("rgb1", r2 / 255.0f, g2 / 255.0f, b2 / 255.0f);
            lastColor2R = r2;
            lastColor2G = g2;
            lastColor2B = b2;
        }

        float stepValue = 300 * step;
        if (stepValue != lastStep) {
            shader.setUniformf("step", stepValue);
            lastStep = stepValue;
        }
        shader.setUniformf("offset", (float) ((((double) System.currentTimeMillis() * (double) speed) % (mc.displayWidth * mc.displayHeight)) / 10.0f));
    }

    public static void setup(float step, float speed, Color color, Color color2) {
        ShaderStateManager.pushState();
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        glDisable(GL_STENCIL_TEST);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        shader.attachShader();
        setupUniforms(step, speed, color, color2);
    }

    public static void finish() {
        shader.releaseShader();
        GlStateManager.popMatrix();
        ShaderStateManager.popState();
    }
}

