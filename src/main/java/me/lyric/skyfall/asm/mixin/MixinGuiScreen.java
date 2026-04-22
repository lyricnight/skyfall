package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.impl.event.mc.GuiMouseClicked;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author lyric
 * @see me.lyric.skyfall.impl.feature.miscellaneous.SlotBinding
 */
@Mixin(value = GuiScreen.class, priority = Integer.MAX_VALUE)
public class MixinGuiScreen {

    //must be CAPTURE_FAILHARD or locals will silently fail
    //this doesn't check if we click inside the inventory itself, meaning it'll pass along a null value - for one of the parameters.
    //TODO test and check which value needs a null check, and figure out how the method fails
    @Inject(method = "handleMouseInput()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;mouseClicked(III)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void mouseClickHook(CallbackInfo ci, int i, int j, int k)
    {
        if (((GuiScreen) (Object) this) instanceof GuiInventory)
        {
            GuiMouseClicked event = new GuiMouseClicked(i, j, k, (GuiInventory) (Object) this);
            EventBus.getInstance().post(event);
            if (event.isCancelled()) ci.cancel();
        }
    }
}
