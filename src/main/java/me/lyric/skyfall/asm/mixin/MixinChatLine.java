package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.MathAnimation;
import me.lyric.skyfall.impl.feature.miscellaneous.Chat;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatLine.class)
public class MixinChatLine implements Globals {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initHook(int i, IChatComponent iChatComponent, int j, CallbackInfo ci)
    {
        Managers.FEATURES.get(Chat.class).animationMap.put(ChatLine.class.cast(this), new MathAnimation(Managers.FEATURES.get(Chat.class).time.getValue(), -mc.fontRendererObj.getStringWidth(iChatComponent.getFormattedText()), 0, false, MathAnimation.AnimationMode.LINEAR));
    }
}
