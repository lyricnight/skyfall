package me.lyric.skyfall.impl.manager;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.api.utils.string.StringUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

/**
 * @author lyric
 * manages messages in chat.
 */
public final class Messages implements Globals {

    public void send(String message) {
        if (Null.is())
        {
            Skyfall.LOGGER.error("send() called when Null is present. Report this!");
            return;
        }
        ChatComponentText component = new ChatComponentText(StringUtils.getSkyfallPrefix() + message);
        mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    public void sendClickable(String message, ClickEvent.Action action, String actionContent) {
        if (Null.is())
        {
            Skyfall.LOGGER.error("sendClickable() called when Null is present. Report this!");
            return;
        }
        ChatComponentText component = new ChatComponentText(StringUtils.getSkyfallPrefix() + message);
        ChatStyle style = new ChatStyle();
        style.setChatClickEvent(new ClickEvent(action, actionContent));
        component.setChatStyle(style);
        mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    public void sendOverwrite(String message, int id) {
        if (Null.is())
        {
            Skyfall.LOGGER.error("sendOverwrite() called when Null is present. Report this!");
            return;
        }
        ChatComponentText component = new ChatComponentText(StringUtils.getSkyfallPrefix() + message);
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(component, id);
    }

    public void sendCommand(String message) {
        if (Null.is())
        {
            Skyfall.LOGGER.error("sendCommand() called when Null is present. Report this!");
            return;
        }
        ChatComponentText component = new ChatComponentText(StringUtils.getSkyfallPrefix() + ChatFormatting.GREEN + "[" + ChatFormatting.BOLD + "CommandHandler" + ChatFormatting.RESET + ChatFormatting.GREEN + "] " + ChatFormatting.RESET + message);
        mc.ingameGUI.getChatGUI().printChatMessage(component);
    }
}
