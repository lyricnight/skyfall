package me.lyric.skyfall.api.utils.shader.framework;

import me.lyric.skyfall.api.utils.maths.MathsUtils;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class UniformBufferCache {

    private static final Map<Integer, FloatBuffer> gaussianWeightCache = new HashMap<>();

    public static FloatBuffer getGaussianWeights(int radius) {
        return gaussianWeightCache.computeIfAbsent(radius, r -> {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
            for (int i = 0; i <= r; i++) {
                buffer.put(MathsUtils.gaussian(i, r / 2.0f));
            }
            buffer.rewind();
            return buffer;
        });
    }

    public static FloatBuffer getGaussianWeights(int radius, float sigma) {
        int key = -(radius * 1000 + (int)(sigma * 100));
        return gaussianWeightCache.computeIfAbsent(key, k -> {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
            for (int i = 0; i <= radius; i++) {
                buffer.put(MathsUtils.gaussian(i, sigma));
            }
            buffer.rewind();
            return buffer;
        });
    }

    /**
     * Clears all cached buffers. Should be called on resource pack reload or mod unload.
     */
    public static void clearAll() {
        gaussianWeightCache.clear();
    }

    /**
     * Gets the number of cached weight buffers.
     */
    public static int getCacheSize() {
        return gaussianWeightCache.size();
    }
}

