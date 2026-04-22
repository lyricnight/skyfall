package me.lyric.skyfall.api.utils.shader;

import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.render.ShaderUtils;
import me.lyric.skyfall.api.utils.shader.framework.FramebufferPool;
import me.lyric.skyfall.api.utils.shader.framework.ShaderStateManager;
import me.lyric.skyfall.api.utils.shader.framework.UniformBufferCache;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.glUniform1;

public class ShadowShader implements Globals {
    private final static ShaderUtils shadowShader = new ShaderUtils("shadow");

    public static void shadow(int radius, int offset, Runnable renderCallback) {
        shadow(radius, offset, offset, renderCallback);
    }

    public static void shadow(int radius, int offsetX, int offsetY, Runnable renderCallback) {
        Framebuffer buffer = FramebufferPool.acquire("shadow_render", false);
        // CRITICAL: Set clear color BEFORE clearing!
        buffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
        buffer.framebufferClear();
        buffer.bindFramebuffer(true);
        renderCallback.run();
        buffer.unbindFramebuffer();
        render(buffer.framebufferTexture, radius, offsetX, offsetY);
        FramebufferPool.release("shadow_render", buffer);
    }

    private static void render(int sourceTexture, int radius, int offsetX, int offsetY) {
        ShaderStateManager.pushState();

        try {
            GlStateManager.pushMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            glDisable(GL_STENCIL_TEST);
            GlStateManager.enableAlpha();
            glAlphaFunc(GL_GREATER, 0.0f);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            final FloatBuffer weightBuffer = UniformBufferCache.getGaussianWeights(radius);
            RenderUtils.setAlphaLimit(0.0F);
            Framebuffer framebuffer = FramebufferPool.acquire("shadow_horizontal", false);
            // CRITICAL: Set clear color BEFORE clearing!
            framebuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            shadowShader.attachShader();
            setupUniforms(radius, offsetX, 0, weightBuffer);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, sourceTexture);
            ShaderUtils.screenTex();
            shadowShader.releaseShader();
            framebuffer.unbindFramebuffer();

            // CRITICAL FIX: Before rendering to main screen, ensure alpha channel is enabled
            // When Minecraft chat is empty, colorMask might have alpha disabled
            GlStateManager.colorMask(true, true, true, true);

            mc.getFramebuffer().bindFramebuffer(true);
            shadowShader.attachShader();
            setupUniforms(radius, 0, offsetY, weightBuffer);
            glActiveTexture(GL_TEXTURE16);
            glBindTexture(GL_TEXTURE_2D, sourceTexture);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
            ShaderUtils.screenTex();
            shadowShader.releaseShader();
            FramebufferPool.release("shadow_horizontal", framebuffer);
            GlStateManager.popMatrix();
        } finally {
            ShaderStateManager.popState();
        }
    }

    private static void setupUniforms(int radius, int directionX, int directionY, FloatBuffer weights) {
        shadowShader.setUniformi("inTexture", 0);
        shadowShader.setUniformi("textureToCheck", 16);
        shadowShader.setUniformf("radius", radius);
        shadowShader.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        shadowShader.setUniformf("direction", directionX, directionY);
        glUniform1(shadowShader.getUniform("weights"), weights);
    }
}

