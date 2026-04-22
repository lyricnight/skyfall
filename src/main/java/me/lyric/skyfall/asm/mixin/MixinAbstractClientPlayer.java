package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.render.Camera;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {
    @Inject(method = "isSpectator()Z", at = @At("HEAD"), cancellable = true)
    private void isSpectator(CallbackInfoReturnable<Boolean> ci) {
        if (Managers.FEATURES.get(Camera.class).shouldOverrideSpectator((AbstractClientPlayer) (Object) this)) {
            ci.setReturnValue(true);
        }
    }
}
