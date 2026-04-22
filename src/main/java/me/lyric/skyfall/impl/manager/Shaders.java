package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.shader.framework.FramebufferPool;
import me.lyric.skyfall.api.utils.shader.framework.ShaderStateManager;
import me.lyric.skyfall.api.utils.shader.framework.UniformBufferCache;

/**
 * @author lyric
 * wrapper class for debug and unload of shader classes
 */
public final class Shaders {
    public void debug()
    {
        Managers.MESSAGES.send(FramebufferPool.getStats());
        Managers.MESSAGES.send("Buffer Cache Size: " + UniformBufferCache.getCacheSize());
        Managers.MESSAGES.send("Shader State Stack Size: " + ShaderStateManager.getStackDepth());
    }

    public void shutdown()
    {
        UniformBufferCache.clearAll();
        FramebufferPool.clearAll();
        ShaderStateManager.clearStack();
    }
}
