package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.render.HandShader;
import me.lyric.skyfall.impl.feature.render.Model;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.*;

@Mixin(value = ItemRenderer.class, priority = Integer.MAX_VALUE)
public abstract class MixinItemRenderer {

    @Shadow
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {}

    @Shadow
    private void doItemUsedTransformations(float swingProgress) {}

    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    public void renderOverlaysHook(float partialTicks, CallbackInfo ci)
    {
        if (Managers.FEATURES.get(HandShader.class).isEnabled()) ci.cancel();
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    private void onPushMatrix(float partialTicks, CallbackInfo ci)
    {
        Model model = Managers.FEATURES.get(Model.class);
        if (model.isEnabled())
        {
            float[] scale = model.getScale();
            glScalef(scale[0], scale[1], scale[2]);
        }
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;doItemUsedTransformations(F)V"))
    private void doItemUsedTransformationsHook(ItemRenderer instance, float swingProgress)
    {
        this.doItemUsedTransformations(swingProgress);
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V"))
    private void transformFirstPersonItemHook(ItemRenderer instance, float equipProgress, float swingProgress)
    {
        Model model = Managers.FEATURES.get(Model.class);
        if (model.isEnabled())
        {
            GlStateManager.translate(
                0.56F + model.getPositionX(),
                -0.52F + model.getPositionY(),
                -0.71999997F + model.getPositionZ()
            );

            GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            float f = (float) Math.sin(swingProgress * swingProgress * Math.PI);
            float f1 = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);
            GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(model.getRotationX(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(model.getRotationY(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(model.getRotationZ(), 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
        }
        else
        {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
    }
}
