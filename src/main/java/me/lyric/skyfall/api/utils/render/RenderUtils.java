package me.lyric.skyfall.api.utils.render;

import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static java.lang.Math.cos;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author various
 * generic render methods that you can find anywhere
 * This is only for UI rendering methods - these methods CAN LEAK GL STATE AND HAVE DIRECT CALLS!
 */
@UtilityClass
public final class RenderUtils implements Globals {
    public static void drawExpand(float x, float y, boolean expanded) {
        y += 3.0f;
        GlStateManager.pushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor(Color.WHITE);
        glBegin(GL_LINE_STRIP);

        if (expanded) {
            glVertex2f(x + 2.5f, y);
            glVertex2f(x + 5.0f, y + 2.5f);
            glVertex2f(x + 7.5f, y);
        } else {
            glVertex2f(x + 5.8333f, y - 1.5f);
            glVertex2f(x + 3.3333f, y + 1.0f);
            glVertex2f(x + 5.8333f, y + 3.5f);
        }

        glEnd();
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        GlStateManager.popMatrix();
    }

    public static void gradient(float x, float y, float width, float height, Color topLeft, Color topRight, Color bottomLeft, Color bottomRight) {
        setupDefault(Color.WHITE);
        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);

        glColor(topLeft);
        glVertex2f(x, y);
        glColor(bottomLeft);
        glVertex2f(x, height);
        glColor(bottomRight);
        glVertex2f(width, height);
        glColor(topRight);
        glVertex2f(width, y);

        glEnd();
        glShadeModel(GL_FLAT);
        glColor(Color.WHITE);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void picker(float x, float y, float width, float height, Color color) {
        GlStateManager.pushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glShadeModel(GL_SMOOTH);
        glBegin(GL_POLYGON);

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glVertex2f(x, y);
        glVertex2f(x, height);

        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
        glVertex2f(width, height);
        glVertex2f(width, y);

        glEnd();
        glDisable(GL_ALPHA_TEST);
        glBegin(GL_POLYGON);

        glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        glVertex2f(x, y);

        glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
        glVertex2f(x, height);
        glVertex2f(width, height);

        glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        glVertex2f(width, y);

        glEnd();
        glEnable(GL_ALPHA_TEST);

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glShadeModel(GL_FLAT);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        GlStateManager.popMatrix();
    }

    public static void circle(float x, float y, final float radius, Color color) {
        final double pi = Math.PI;
        x = x - radius / 2.0f;
        y = y - radius / 2.0f;

        GlStateManager.pushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glDisable(GL_ALPHA_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glShadeModel(GL_SMOOTH);
        glColor(color);
        glDisable(GL_CULL_FACE);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i < 360; i++) {
            glVertex2d(x + radius + Math.sin(i * pi / 180.0) * radius * -1.0, y + radius + Math.cos(i * pi / 180.0) * radius * -1.0);
        }

        glEnd();
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        GlStateManager.popMatrix();
    }

    public static void invokeScale(float scale) {
        GlStateManager.pushMatrix();
        glScalef(scale, scale, scale);
    }

    public static void resetScale() {
        GlStateManager.popMatrix();
    }

    public static void prepareScissor(float x, float y, float width, float height) {
        scissor(x, y, width, height);
        glEnable(GL_SCISSOR_TEST);
    }

    public static void scissor(float x, float y, float width, float height) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        glScissor((int) (x * scaledResolution.getScaleFactor()), (int) ((scaledResolution.getScaledHeight() - height) * scaledResolution.getScaleFactor()), (int) ((width - x) * scaledResolution.getScaleFactor()), (int) ((height - y) * scaledResolution.getScaleFactor()));
    }

    public static void releaseScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void setAlphaLimit(float limit) {
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static void rounded(float x, float y, float width, float height, float radius, Color color) {
        setupDefault(color);
        glBegin(GL_TRIANGLE_FAN);
        for (int i = 1; i <= 4; i++) {
            corner(x, y, width, height, radius, i);
        }
        releaseDefault();
    }

    public static void roundedOutline(float x, float y, float width, float height, float radius, Color color) {
        setupDefault(color);
        glLineWidth(1.0f);
        glBegin(GL_LINE_STRIP);
        for (int i = 1; i <= 4; i++) {
            corner(x, y, width, height, radius, i);
        }
        glVertex2d(
                x + radius + Math.sin(0 * Math.PI / 180.0f) * radius * -1.0f,
                y + radius + cos(0 * Math.PI / 180.0f) * radius * -1.0f
        );
        releaseDefault();
    }

    public static void corner(float x, float y, float width, float height, float radius, int corner) {
        double pi = Math.PI;
        int i = 0;
        switch (corner) {
            case 1:
                // Top left
                while (i < 90) {
                    glVertex2d(x + radius + Math.sin(i * pi / 180.0f) * radius * -1.0f, y + radius + Math.cos(i * pi / 180.0) * radius * -1.0f);
                    i++;
                }
                break;
            case 2:
                // Bottom Left
                i = 90;
                while (i < 180) {
                    glVertex2d(
                            x + radius + Math.sin(i * pi / 180.0f) * radius * -1.0f,
                            height - radius + Math.cos(i * pi / 180.0f) * radius * -1.0f
                    );
                    i++;
                }
                break;
            case 3:
                // Bottom Right
                while (i < 90) {
                    glVertex2d(
                            width - radius + Math.sin(i * pi / 180.0) * radius,
                            height - radius + Math.cos(i * pi / 180.0) * radius
                    );
                    i++;
                }
                break;
            case 4:
                // Top Right
                i = 90;
                while (i < 180) {
                    glVertex2d(
                            width - radius + Math.sin(i * pi / 180.0f) * radius,
                            y + radius + Math.cos(i * pi / 180.0f) * radius
                    );
                    i++;
                }
                break;
        }
    }


    public static void setupDefault(Color color) {
        GlStateManager.pushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor(color);
    }

    public static void releaseDefault() {
        glEnd();
        glColor(Color.WHITE);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void texture(float x, float y, float width, float height, Color color, ResourceLocation resourceLocation) {
        bind(resourceLocation);
        GlStateManager.pushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor(color);
        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(x, y);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(x, height);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(width, height);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(width, y);

        glEnd();
        glColor(Color.WHITE);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void textureSmooth(float x, float y, float width, float height, Color color, ResourceLocation resourceLocation)
    {
        bind(resourceLocation);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GlStateManager.pushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor(color);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(x, y);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(x, height);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(width, height);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(width, y);
        glEnd();
        glColor(Color.WHITE);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void rect(float x, float y, float width, float height, Color color) {
        setupDefault(color);
        glBegin(GL_QUADS);

        glVertex2f(x, y);
        glVertex2f(x, height);
        glVertex2f(width, height);
        glVertex2f(width, y);

        releaseDefault();
    }

    public static void bind(final ResourceLocation resourceLocation) {
        mc.getTextureManager().bindTexture(resourceLocation);
    }

    public static void glColor(final Color color) {
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }
}
