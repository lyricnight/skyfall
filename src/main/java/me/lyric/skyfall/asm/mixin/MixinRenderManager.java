package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.ducks.IRenderManager;
import me.lyric.skyfall.impl.event.mc.RenderCheckEvent;
import me.lyric.skyfall.impl.feature.render.Camera;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author lyric
 */
@Mixin(RenderManager.class)
public abstract class MixinRenderManager implements IRenderManager {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void shouldRenderHook(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir)
    {
        RenderCheckEvent event = RenderCheckEvent.pooled(entityIn);
        EventBus.getInstance().post(event);
        if (event.isCancelled())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z", at = @At("HEAD"))
    private void beforeRenderEntitySimple(Entity entity, float partialTicks, CallbackInfoReturnable<Boolean> ci) {
        Managers.FEATURES.get(Camera.class).onBeforeRenderEntity(entity);
    }

    @Inject(method = "renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z", at = @At("TAIL"))
    private void afterRenderEntitySimple(Entity entity, float partialTicks, CallbackInfoReturnable<Boolean> ci) {
        Managers.FEATURES.get(Camera.class).onAfterRenderEntity(entity);
    }

    @Accessor("renderPosX")
    public abstract double getRenderPosX();

    @Accessor("renderPosY")
    public abstract double getRenderPosY();

    @Accessor("renderPosZ")
    public abstract double getRenderPosZ();
}
