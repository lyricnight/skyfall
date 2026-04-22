package me.lyric.skyfall.api.utils.string;

import com.mojang.realmsclient.gui.ChatFormatting;
import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.internals.Interface;

@UtilityClass
public class StringUtils {

    /**
     * wow.
     * @return client prefix used by @link Messages
     */
    public String getSkyfallPrefix()
    {
        String bracketStyle = Managers.FEATURES.get(Interface.class).boldBracket.getValue() ? "§l" : "";
        String nameStyle = Managers.FEATURES.get(Interface.class).boldName.getValue() ? "§l" : "";

        return resolveColourCode(Managers.FEATURES.get(Interface.class).internalCode2.getValue()) +
                bracketStyle +
                "<< " +
                resolveColourCode(Managers.FEATURES.get(Interface.class).internalCode.getValue()) +
                nameStyle +
                "Skyfall " +
                resolveColourCode(Managers.FEATURES.get(Interface.class).internalCode2.getValue()) +
                bracketStyle +
                ">> " +
                ChatFormatting.RESET;
    }

    /**
     * resolves a string to a ChatFormatting enum.
     * @param in - string to resolve
     * @return ChatFormatting enum corresponding to string
     * @throws IllegalStateException - if string is not valid
     */
    public ChatFormatting resolveColourCode(String in)
    {
        switch (in)
        {
            case "None":
                return ChatFormatting.RESET;
            case "Black":
                return ChatFormatting.BLACK;
            case "DarkGray":
                return ChatFormatting.DARK_GRAY;
            case "Gray":
                return ChatFormatting.GRAY;
            case "DarkBlue":
                return ChatFormatting.DARK_BLUE;
            case "Blue":
                return ChatFormatting.BLUE;
            case "DarkGreen":
                return ChatFormatting.DARK_GREEN;
            case "Green":
                return ChatFormatting.GREEN;
            case "DarkAqua":
                return ChatFormatting.DARK_AQUA;
            case "Aqua":
                return ChatFormatting.AQUA;
            case "DarkRed":
                return ChatFormatting.DARK_RED;
            case "Red":
                return ChatFormatting.RED;
            case "DarkPurple":
                return ChatFormatting.DARK_PURPLE;
            case "Purple":
                return ChatFormatting.LIGHT_PURPLE;
            case "Gold":
                return ChatFormatting.GOLD;
            case "Yellow":
                return ChatFormatting.YELLOW;
        }
        throw new IllegalStateException("Unexpected value: " + in);
    }

    /**
     * Removes formatting from a string.
     * @param text - string to remove formatting from
     * @return string without formatting
     */
    public static String removeFormatting(String text) {
        return text.replaceAll("[§&][0-9a-fk-or]", "");
    }
}
