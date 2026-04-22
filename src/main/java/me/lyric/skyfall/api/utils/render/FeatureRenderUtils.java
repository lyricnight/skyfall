package me.lyric.skyfall.api.utils.render;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author lyric
 * this class holds various rendering utilities for use in features - ie they don't leak GL state.
 * USE THIS FOR ANY NON-GUI RENDERING
 */

public final class FeatureRenderUtils implements Globals {
    private static final Tessellator TESSELLATOR = Tessellator.getInstance();
    private static final WorldRenderer WORLD_RENDERER = TESSELLATOR.getWorldRenderer();
    private static final RenderManager RENDER_MANAGER = mc.getRenderManager();
    
    public static void renderStringInWorld(String text, double x, double y, double z, Color color, int fontSize) {
        double dx = x - RENDER_MANAGER.viewerPosX;
        double dy = y - RENDER_MANAGER.viewerPosY;
        double dz = z - RENDER_MANAGER.viewerPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, dz);
        GlStateManager.rotate(-RENDER_MANAGER.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(RENDER_MANAGER.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        //@turtle what the actual fuck
        float width = Managers.TEXT.hudStringWidth(text, fontSize) / 2;        Managers.TEXT.hudString(text, -width, 0, color, fontSize);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawLine(Double x, Double y, Double z, Double x2, Double y2, Double z2, Color color, float thickness, boolean phase) {
        GlStateManager.disableLighting();
        GL11.glBlendFunc(770, 771);
        GlStateManager.enableBlend();
        GL11.glLineWidth(thickness);
        if (phase) GlStateManager.enableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-RENDER_MANAGER.viewerPosX, -RENDER_MANAGER.viewerPosY, -RENDER_MANAGER.viewerPosZ);
        WORLD_RENDERER.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        WORLD_RENDERER.pos(x, y, z).endVertex();
        WORLD_RENDERER.pos(x2, y2, z2).endVertex();
        TESSELLATOR.draw();
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    public static void renderBox(Double x, Double y, Double z, Double xWidth, Double yHeight, Double zWidth, Color color, Color fillColor, Float thickness, Boolean phase, Boolean relocate, Boolean filled) {
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(thickness);
        if (phase) GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();

        GlStateManager.pushMatrix();

        if (relocate) GlStateManager.translate(-RENDER_MANAGER.viewerPosX, -RENDER_MANAGER.viewerPosY, -RENDER_MANAGER.viewerPosZ);

        if (filled) {
            GlStateManager.color(fillColor.getRed() / 255f, fillColor.getGreen() / 255f,fillColor.getBlue() / 255f, fillColor.getAlpha() / 255f);
            WORLD_RENDERER.begin(7, DefaultVertexFormats.POSITION_NORMAL);
            Double maxX = x + xWidth;
            Double maxY = y + yHeight;
            Double maxZ = z + zWidth;

            addVertex(x, maxY, z, 0f, 0f, -1f);
            addVertex(maxX, maxY, z, 0f, 0f, -1f);
            addVertex(maxX, y, z, 0f, 0f, -1f);
            addVertex(x, y, z, 0f, 0f, -1f);

            addVertex(x, y, maxZ, 0f, 0f, 1f);
            addVertex(maxX, y, maxZ, 0f, 0f, 1f);
            addVertex(maxX, maxY, maxZ, 0f, 0f, 1f);
            addVertex(x, maxY, maxZ, 0f, 0f, 1f);

            addVertex(x, y, z, 0f, -1f, 0f);
            addVertex(maxX, y, z, 0f, -1f, 0f);
            addVertex(maxX, y, maxZ, 0f, -1f, 0f);
            addVertex(x, y, maxZ, 0f, -1f, 0f);

            addVertex(x, maxY, maxZ, 0f, 1f, 0f);
            addVertex(maxX, maxY, maxZ, 0f, 1f, 0f);
            addVertex(maxX, maxY, z, 0f, 1f, 0f);
            addVertex(x, maxY, z, 0f, 1f, 0f);

            addVertex(x, y, maxZ, -1f, 0f, 0f);
            addVertex(x, maxY, maxZ, -1f, 0f, 0f);
            addVertex(x, maxY, z, -1f, 0f, 0f);
            addVertex(x, y, z, -1f, 0f, 0f);

            addVertex(maxX, y, z, 1f, 0f, 0f);
            addVertex(maxX, maxY, z, 1f, 0f, 0f);
            addVertex(maxX, maxY, maxZ, 1f, 0f, 0f);
            addVertex(maxX, y, maxZ, 1f, 0f, 0f);

            TESSELLATOR.draw();
        }
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f,color.getBlue() / 255f, color.getAlpha() / 255f);
        GL11.glLineWidth(thickness);
        WORLD_RENDERER.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        WORLD_RENDERER.pos(x+xWidth,y+yHeight,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y+yHeight,z).endVertex();
        WORLD_RENDERER.pos(x,y+yHeight,z).endVertex();
        WORLD_RENDERER.pos(x,y+yHeight,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y+yHeight,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y,z).endVertex();
        WORLD_RENDERER.pos(x,y,z).endVertex();
        WORLD_RENDERER.pos(x,y,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x,y,z).endVertex();
        WORLD_RENDERER.pos(x,y+yHeight,z).endVertex();
        WORLD_RENDERER.pos(x,y,z).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y,z).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y+yHeight,z).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y,z).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x,y,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x,y+yHeight,z+zWidth).endVertex();
        WORLD_RENDERER.pos(x+xWidth,y+yHeight,z+zWidth).endVertex();

        TESSELLATOR.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    private static void addVertex(Double x, Double y, Double z, Float nx, Float ny, Float nz) {
        WORLD_RENDERER.pos(x, y, z).normal(nx, ny, nz).endVertex();
    }

    public static void drawBorderedRect(float x, float y, float x2, float y2, float lineSize, int color, int borderColor) {
        drawRect(x, y, x2, y2, color);
        drawRect(x, y, x + lineSize, y2, borderColor);
        drawRect(x2 - lineSize, y, x2, y2, borderColor);
        drawRect(x, y2 - lineSize, x2, y2, borderColor);
        drawRect(x, y, x2, y + lineSize, borderColor);
    }

    public static void drawRect(float startX, float startY, float endX, float endY, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        WORLD_RENDERER.begin(7, DefaultVertexFormats.POSITION_COLOR);
        WORLD_RENDERER.pos(startX, endY, 0.0D).color(red, green, blue, alpha).endVertex();
        WORLD_RENDERER.pos(endX, endY, 0.0D).color(red, green, blue, alpha).endVertex();
        WORLD_RENDERER.pos(endX, startY, 0.0D).color(red, green, blue, alpha).endVertex();
        WORLD_RENDERER.pos(startX, startY, 0.0D).color(red, green, blue, alpha).endVertex();
        TESSELLATOR.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
