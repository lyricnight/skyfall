package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.render.NoRender;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderItemFrame.class)
public class MixinRenderItemFrame {
    @ModifyArg(method = "renderName(Lnet/minecraft/entity/item/EntityItemFrame;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;color(FFFF)Lnet/minecraft/client/renderer/WorldRenderer;"), index = 3)
    private float skyfall$removeBackground(float alpha)
    {
        if (Managers.FEATURES.get(NoRender.class).isEnabled() && Managers.FEATURES.get(NoRender.class).backgroundNametags.getValue()) {
            return 0.0f;
        }
        else return alpha;
    }
}
