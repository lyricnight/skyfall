package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.render.NoRender;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity {
    @Unique
    private boolean skyfall$skipNametagBackground = false;

    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;begin(ILnet/minecraft/client/renderer/vertex/VertexFormat;)V"))
    private void redirectWorldRendererBegin(WorldRenderer worldRenderer, int glMode, VertexFormat format) {
        skyfall$skipNametagBackground = Managers.FEATURES.get(NoRender.class).isEnabled() && Managers.FEATURES.get(NoRender.class).backgroundNametags.getValue();
        if (!skyfall$skipNametagBackground) {
            worldRenderer.begin(glMode, format);
        }
    }

    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;pos(DDD)Lnet/minecraft/client/renderer/WorldRenderer;"))
    private WorldRenderer redirectWorldRendererPos(WorldRenderer worldRenderer, double x, double y, double z) {
        if (skyfall$skipNametagBackground) {
            return worldRenderer;
        }
        return worldRenderer.pos(x, y, z);
    }

    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;color(FFFF)Lnet/minecraft/client/renderer/WorldRenderer;"))
    private WorldRenderer redirectWorldRendererColor(WorldRenderer worldRenderer, float r, float g, float b, float a) {
        if (skyfall$skipNametagBackground) {
            return worldRenderer;
        }
        return worldRenderer.color(r, g, b, a);
    }

    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;endVertex()V"))
    private void redirectWorldRendererEndVertex(WorldRenderer worldRenderer) {
        if (!skyfall$skipNametagBackground) {
            worldRenderer.endVertex();
        }
    }

    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()V"))
    private void redirectTessellatorDraw(Tessellator tessellator) {
        if (!skyfall$skipNametagBackground) {
            tessellator.draw();
        }
    }
}
