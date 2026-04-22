package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.MathAnimation;
import me.lyric.skyfall.impl.feature.miscellaneous.Chat;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat implements Globals {

    @Unique
    private ChatLine skyfall$currentLine;

    @SuppressWarnings("ALL")
    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V", ordinal = 0))
    public void drawRectHook(int i, int j, int k, int l, int m)
    {
        if (Managers.FEATURES.get(Chat.class).isEnabled() && Managers.FEATURES.get(Chat.class).clean.getValue())
        {
            //this is necessary to prevent the background from rendering
        }
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2, remap = false))
    public int chatLinesSizeHook(List<?> instance)
    {
        if (Managers.FEATURES.get(Chat.class).isEnabled() && Managers.FEATURES.get(Chat.class).infinite.getValue())
        {
            return -2147483647;
        }
        else return instance.size();
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0, remap = false))
    public int drawnChatLinesSizeHook(List<?> instance)
    {
        if (Managers.FEATURES.get(Chat.class).isEnabled() && Managers.FEATURES.get(Chat.class).infinite.getValue())
        {
            return -2147483647;
        }
        else return instance.size();
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    public int drawStringWithShadowHook(FontRenderer instance, String text, float x, float y, int color)
    {
        MathAnimation animation = null;
        Chat CHAT = Managers.FEATURES.get(Chat.class);
        if (skyfall$currentLine != null)
        {
            if (CHAT.animationMap.containsKey(skyfall$currentLine))
            {
                animation = CHAT.animationMap.get(skyfall$currentLine);
            }
            if (animation != null)
            {
                animation.add();
            }
        }
        return instance.drawStringWithShadow(text, (float) (x + ((animation != null && CHAT.isEnabled() && CHAT.animated.getValue()) ? animation.getCurrent() : 0)), y, color);
    }

    // turtle-worthy code
    @Inject(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ChatLine;getUpdatedCounter()I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void drawChatHook(int updateCounter, CallbackInfo ci, int i, boolean bl, int j, int k, float f, float g, int l, int m, ChatLine chatLine) {
        skyfall$currentLine = chatLine;
    }
}
