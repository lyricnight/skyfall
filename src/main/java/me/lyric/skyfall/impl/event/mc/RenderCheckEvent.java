package me.lyric.skyfall.impl.event.mc;

import lombok.Getter;
import lombok.Setter;
import me.lyric.skyfall.api.event.Event;
import net.minecraft.entity.Entity;

/**
 * @author lyric
 * @see me.lyric.skyfall.asm.mixin.MixinRenderManager
 * this is a POOLED event - there is no storing this event, and so it only has 1 instance
 */
public class RenderCheckEvent extends Event {
    /**
     * Pooled instance to avoid allocation every entity render check
     */
    private static final RenderCheckEvent POOLED = new RenderCheckEvent();

    /**
     * we only need this for now, since all we need to get is the entity we're rendering, not its position.
     */
    @Getter
    @Setter
    private Entity entity;

    private RenderCheckEvent() {
        this.entity = null;
    }

    public static RenderCheckEvent pooled(Entity entity) {
        POOLED.entity = entity;
        POOLED.setCancelled(false);
        POOLED.setHandled(false);
        return POOLED;
    }
}
