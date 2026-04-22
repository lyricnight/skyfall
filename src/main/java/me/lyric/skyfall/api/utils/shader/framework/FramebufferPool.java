package me.lyric.skyfall.api.utils.shader.framework;

import me.lyric.skyfall.api.utils.interfaces.Globals;
import net.minecraft.client.shader.Framebuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Pools framebuffers
 * @author lyric
 */
public class FramebufferPool implements Globals {

    private static final Map<String, Stack<Framebuffer>> pools = new HashMap<>();
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    /**
     * Acquires a framebuffer from the pool, or creates a new one if none available.
     *
     * @param name Pool name (e.g., "blur", "shadow", "temp")
     * @param useDepth Whether the framebuffer should have a depth buffer
     * @return A framebuffer ready to use
     */
    public static Framebuffer acquire(String name, boolean useDepth) {
        checkResize();
        Stack<Framebuffer> pool = pools.computeIfAbsent(name, k -> new Stack<>());

        Framebuffer fb;
        if (!pool.isEmpty()) {
            fb = pool.pop();
            fb.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

            if (fb.framebufferWidth != mc.displayWidth || fb.framebufferHeight != mc.displayHeight) {
                fb.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
            }
        } else {
            fb = new Framebuffer(mc.displayWidth, mc.displayHeight, useDepth);
            fb.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
        }

        return fb;
    }
    /**
     * Returns a framebuffer to the pool for reuse.
     *
     * @param name - name
     * @param framebuffer - framebuffer to return
     */
    public static void release(String name, Framebuffer framebuffer) {
        if (framebuffer == null) return;
        Stack<Framebuffer> pool = pools.computeIfAbsent(name, k -> new Stack<>());
        if (pool.size() < 3) {
            pool.push(framebuffer);
        } else {
            framebuffer.deleteFramebuffer();
        }
    }

    /**
     * Clears all framebuffers from a specific pool.
     */
    public static void clearPool(String name) {
        Stack<Framebuffer> pool = pools.get(name);
        if (pool != null) {
            while (!pool.isEmpty()) {
                pool.pop().deleteFramebuffer();
            }
        }
    }

    /**
     * Clears all framebuffers from all pools.
     */
    public static void clearAll() {
        for (Stack<Framebuffer> pool : pools.values()) {
            while (!pool.isEmpty()) {
                pool.pop().deleteFramebuffer();
            }
        }
        pools.clear();
    }

    /**
     * Checks if the window was resized
     */
    private static void checkResize() {
        if (mc.displayWidth != lastWidth || mc.displayHeight != lastHeight) {
            lastWidth = mc.displayWidth;
            lastHeight = mc.displayHeight;
            clearAll();
        }
    }

    /**
     * Gets the total number of pooled framebuffers.
     */
    public static int getTotalPooledFramebuffers() {
        return pools.values().stream().mapToInt(Stack::size).sum();
    }


    public static String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("FramebufferPool Statistics:\n");
        sb.append("  Resolution: ").append(lastWidth).append("x").append(lastHeight).append("\n");
        sb.append("  Total Pools: ").append(pools.size()).append("\n");
        sb.append("  Total Framebuffers: ").append(getTotalPooledFramebuffers()).append("\n");
        for (Map.Entry<String, Stack<Framebuffer>> entry : pools.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append("\n");
        }
        return sb.toString();
    }
}

