package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.player.PlayerScale;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author lyric
 * @link PlayerScale
 */
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {
    @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V", at = @At("TAIL"))
    public void onPreRenderCallbackTail(AbstractClientPlayer entitylivingbaseIn, float partialTickTime, CallbackInfo ci)
    {
        Managers.FEATURES.get(PlayerScale.class).onScalePlayerHook(entitylivingbaseIn);
    }
}
