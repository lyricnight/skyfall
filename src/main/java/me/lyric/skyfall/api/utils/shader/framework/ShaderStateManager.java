package me.lyric.skyfall.api.utils.shader.framework;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Extends mc GlStateManager
 * Uses GlStateManager for Blend state, depth test, alpha test, textures, lighting, color
 * Adds management for shader programs and active texture units
 * @author lyric
 */
public class ShaderStateManager {
    private static final Stack<ShaderState> stateStack = new Stack<>();

    public static class ShaderState {
        int shaderProgram;
        int activeTexture;
        int[] textureBindings = new int[32];

        boolean blendEnabled;
        int blendSrcRGB, blendDstRGB, blendSrcAlpha, blendDstAlpha;
        boolean depthTest, alphaTest, stencilTest;
        boolean texture2D;
        float[] color = new float[4];

        public void capture() {
            shaderProgram = glGetInteger(GL_CURRENT_PROGRAM);
            activeTexture = glGetInteger(GL_ACTIVE_TEXTURE);
            int savedActiveTexture = activeTexture;
            for (int i = 0; i < 32; i++) {
                glActiveTexture(GL_TEXTURE0 + i);
                textureBindings[i] = glGetInteger(GL_TEXTURE_BINDING_2D);
            }
            glActiveTexture(savedActiveTexture);

            blendEnabled = glIsEnabled(GL_BLEND);
            if (blendEnabled) {
                IntBuffer buf = BufferUtils.createIntBuffer(16);
                glGetInteger(GL_BLEND_SRC_RGB, buf);
                blendSrcRGB = buf.get(0);
                buf.clear();
                glGetInteger(GL_BLEND_DST_RGB, buf);
                blendDstRGB = buf.get(0);
                buf.clear();
                glGetInteger(GL_BLEND_SRC_ALPHA, buf);
                blendSrcAlpha = buf.get(0);
                buf.clear();
                glGetInteger(GL_BLEND_DST_ALPHA, buf);
                blendDstAlpha = buf.get(0);
            }

            depthTest = glIsEnabled(GL_DEPTH_TEST);
            alphaTest = glIsEnabled(GL_ALPHA_TEST);
            stencilTest = glIsEnabled(GL_STENCIL_TEST);
            texture2D = glIsEnabled(GL_TEXTURE_2D);
            FloatBuffer colorBuf = BufferUtils.createFloatBuffer(16);
            glGetFloat(GL_CURRENT_COLOR, colorBuf);
            colorBuf.get(color);
        }

        public void restore() {
            glUseProgram(shaderProgram);

            for (int i = 0; i < 32; i++) {
                if (textureBindings[i] != 0) {
                    glActiveTexture(GL_TEXTURE0 + i);
                    glBindTexture(GL_TEXTURE_2D, textureBindings[i]);
                }
            }
            glActiveTexture(activeTexture);

            if (blendEnabled) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(blendSrcRGB, blendDstRGB, blendSrcAlpha, blendDstAlpha);
            } else {
                GlStateManager.disableBlend();
            }

            if (depthTest) {
                GlStateManager.enableDepth();
            } else {
                GlStateManager.disableDepth();
            }

            if (alphaTest) {
                GlStateManager.enableAlpha();
            } else {
                GlStateManager.disableAlpha();
            }

            if (stencilTest) {
                glEnable(GL_STENCIL_TEST);
            } else {
                glDisable(GL_STENCIL_TEST);
            }

            if (texture2D) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }

            GlStateManager.color(color[0], color[1], color[2], color[3]);
        }
    }

    /**
     * Pushes current shader state onto the stack.
     */
    public static void pushState() {
        ShaderState state = new ShaderState();
        state.capture();
        stateStack.push(state);
    }

    /**
     * Pops and restores the most recent shader state from the stack.
     */
    public static void popState() {
        if (!stateStack.isEmpty()) {
            stateStack.pop().restore();
        }
    }


    public static void clearStack() {
        stateStack.clear();
    }

    public static int getStackDepth() {
        return stateStack.size();
    }
}

