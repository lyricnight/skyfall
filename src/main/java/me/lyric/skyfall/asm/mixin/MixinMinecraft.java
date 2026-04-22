package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.event.mc.ClickEvent;
import me.lyric.skyfall.impl.event.mc.KeyEvent;
import me.lyric.skyfall.impl.feature.internals.Interface;
import me.lyric.skyfall.impl.feature.internals.MainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = Integer.MAX_VALUE)
public class MixinMinecraft {

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public GuiScreen currentScreen;

    @Inject(method = "dispatchKeypresses", at = @At(value = "HEAD"))
    public void keypresses(CallbackInfo ci)
    {
        int key = Keyboard.getEventKey() == Keyboard.KEY_NONE ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) {
            KeyEvent event = new KeyEvent(key);
            EventBus.getInstance().post(event);
        }
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void onLeftClick(CallbackInfo ci) {
        ClickEvent.LeftClick event = new ClickEvent.LeftClick();
        EventBus.getInstance().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER), cancellable = true)
    private void onRightClick(CallbackInfo ci) {
        ClickEvent.RightClick event = new ClickEvent.RightClick();
        EventBus.getInstance().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"), cancellable = true)
    private void onMiddleClick(CallbackInfo ci) {
        ClickEvent.MiddleClick event = new ClickEvent.MiddleClick();
        EventBus.getInstance().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getLimitFramerate", at = @At("HEAD"), cancellable = true)
    public void onLimitFPS(CallbackInfoReturnable<Integer> cir)
    {
        if (this.theWorld == null && this.currentScreen != null)
        {
            cir.setReturnValue(Managers.FEATURES.get(MainMenu.class).fps.getValue());
        }
    }


    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z", shift = At.Shift.AFTER, ordinal = 0))
    public void onTickUseItem(CallbackInfo ci) {
        Managers.CLICKER.onUseItemTick();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    public void onTickPre(CallbackInfo ci)
    {
        Managers.SERVER.onTickPre();
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    public void tickHookPost(CallbackInfo ci)
    {
        Managers.SERVER.onTickPost();
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    public void shutdownHook(CallbackInfo ci)
    {
        Managers.unload();
    }
}
