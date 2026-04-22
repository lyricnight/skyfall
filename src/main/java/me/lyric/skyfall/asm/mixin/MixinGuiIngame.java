package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.render.Crosshair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {
    @Shadow
    protected abstract boolean showCrosshair();

    @Shadow
    @Final
    protected Minecraft mc;

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;showCrosshair()Z"))
    private boolean redirectShowCrosshair(GuiIngame instance) {
        if (Managers.FEATURES.get(Crosshair.class).isEnabled()) {
            return false;
        }
        return showCrosshair();
    }
}
