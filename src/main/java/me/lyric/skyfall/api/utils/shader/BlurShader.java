package me.lyric.skyfall.api.utils.shader;

import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.ShaderUtils;
import me.lyric.skyfall.api.utils.render.StencilUtils;
import me.lyric.skyfall.api.utils.shader.framework.FramebufferPool;
import me.lyric.skyfall.api.utils.shader.framework.ShaderStateManager;
import me.lyric.skyfall.api.utils.shader.framework.UniformBufferCache;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUniform1;

public class BlurShader implements Globals {
    private static final ShaderUtils blurShader = new ShaderUtils("blur");

    public static void blur(float radius, Runnable renderCallback) {
        StencilUtils.writeStencil();
        renderCallback.run();
        StencilUtils.stencil(1);
        renderBlur(radius);
        StencilUtils.disableStencil();
    }

    private static void renderBlur(float radius) {
        ShaderStateManager.pushState();

        try {
            GlStateManager.pushMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Framebuffer framebuffer = FramebufferPool.acquire("blur", false);
            framebuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            blurShader.attachShader();
            setupUniforms(1, 0, radius);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
            ShaderUtils.screenTex();
            framebuffer.unbindFramebuffer();
            blurShader.releaseShader();
            GlStateManager.colorMask(true, true, true, true);
            mc.getFramebuffer().bindFramebuffer(true);
            blurShader.attachShader();
            setupUniforms(0, 1, radius);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
            ShaderUtils.screenTex();
            blurShader.releaseShader();
            FramebufferPool.release("blur", framebuffer);
            GlStateManager.popMatrix();
        } finally {
            ShaderStateManager.popState();
        }
    }

    private static void setupUniforms(float dir1, float dir2, float radius) {
        blurShader.setUniformi("textureIn", 0);
        blurShader.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        blurShader.setUniformf("direction", dir1, dir2);
        blurShader.setUniformf("radius", radius);
        final FloatBuffer weightBuffer = UniformBufferCache.getGaussianWeights((int) radius);
        glUniform1(blurShader.getUniform("weights"), weightBuffer);
    }
}

