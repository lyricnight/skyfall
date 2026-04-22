package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.ducks.IEntityRenderer;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.feature.render.*;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract class MixinEntityRenderer implements IResourceManagerReloadListener, IEntityRenderer {

    @Shadow
    @Final
    private int[] lightmapColors;


    @Shadow
    protected abstract void renderHand(float partialTicks, int xOffset);

    @Override
    @Accessor("lightmapUpdateNeeded")
    public abstract void lightMapUpdate(boolean val);

    @Override
    public void skyfall$invokeRenderHand(float partialTicks, int pass) {
        renderHand(partialTicks, pass);
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MouseHelper;mouseXYChange()V"))
    public void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        if (Managers.FEATURES.get(Camera.class).isEnabled()) {
            Managers.FEATURES.get(Camera.class).updateCamera();
        }
    }

    @SuppressWarnings("DiscouragedShift")
    @Inject(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V", shift = At.Shift.BEFORE))
    public void updateLightmapHook(float partialTicks, CallbackInfo ci)
    {
        if (Managers.FEATURES.get(Ambience.class).isEnabled())
        {
            Color ambientColor = Managers.FEATURES.get(Ambience.class).getColor();
            float modifier = ambientColor.getAlpha() / 255.0f;
            float ambR = ambientColor.getRed() / 255.0f;
            float ambG = ambientColor.getGreen() / 255.0f;
            float ambB = ambientColor.getBlue() / 255.0f;

            for (int i = 0; i < lightmapColors.length; i++)
            {
                int color = lightmapColors[i];
                int b = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int r = color & 0xFF;
                float valR = r / 255.0f;
                float valG = g / 255.0f;
                float valB = b / 255.0f;

                float finalR = valR + (ambR - valR) * modifier;
                float finalG = valG + (ambG - valG) * modifier;
                float finalB = valB + (ambB - valB) * modifier;

                int red = (int) (finalR * 255);
                int green = (int) (finalG * 255);
                int blue = (int) (finalB * 255);
                lightmapColors[i] = -16777216 | red << 16 | green << 8 | blue;
            }
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void onHurtCamRender(float partialTicks, CallbackInfo ci) {
        if (Managers.FEATURES.get(NoRender.class).isEnabled() && Managers.FEATURES.get(NoRender.class).hurtCam.getValue())
            ci.cancel();
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    public void beforeRenderWorld(float partialTicks, long nanoTime, CallbackInfo ci) {
        Managers.FEATURES.get(Camera.class).onBeforeRenderWorld();
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    public void afterRenderWorld(float partialTicks, long nanoTime, CallbackInfo ci) {
        Managers.FEATURES.get(Camera.class).onAfterRenderWorld();
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onSetupCameraTransform(float fovy, float aspect, float zNear, float zFar) {
        if (Managers.FEATURES.get(Aspect.class).isEnabled())
        {
            Project.gluPerspective(fovy, Managers.FEATURES.get(Aspect.class).aspect.getValue(), zNear, zFar);
        }
        else {
            Project.gluPerspective(fovy, aspect, zNear, zFar);
        }
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderWorldPass(float fovy, float aspect, float zNear, float zFar) {
        if (Managers.FEATURES.get(Aspect.class).isEnabled())
        {
            Project.gluPerspective(fovy, Managers.FEATURES.get(Aspect.class).aspect.getValue(), zNear, zFar);
        }
        else {
            Project.gluPerspective(fovy, aspect, zNear, zFar);
        }
    }

    @Redirect(method = "renderCloudsCheck", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderCloudsCheck(float fovy, float aspect, float zNear, float zFar) {
        if (Managers.FEATURES.get(Aspect.class).isEnabled())
        {
            Project.gluPerspective(fovy, Managers.FEATURES.get(Aspect.class).aspect.getValue(), zNear, zFar);
        }
        else {
            Project.gluPerspective(fovy, aspect, zNear, zFar);
        }
    }

    /**
     * could use eventbus but nah.
     * @param partialTicks - lala
     * @param finishTimeNano - lala
     * @param ci - going to get my money right
     */
    @Inject(method = "renderWorld", at = @At("RETURN"))
    public void renderWorldHookReturn(float partialTicks, long finishTimeNano, CallbackInfo ci)
    {
        if (!Null.is() && Managers.FEATURES.get(HandShader.class).isEnabled()) {
            Managers.FEATURES.get(HandShader.class).onRenderWorld();
        }
    }
}
