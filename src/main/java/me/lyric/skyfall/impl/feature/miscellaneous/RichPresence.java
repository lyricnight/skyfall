package me.lyric.skyfall.impl.feature.miscellaneous;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.api.setting.types.StringSetting;
import me.lyric.skyfall.impl.hud.Spotify;

import java.util.Arrays;

/**
 * @author lyric
 */
public final class RichPresence extends Feature {

    public BooleanSetting custom = setting("Custom", false).invokeTab("Modes");

    public StringSetting detail = setting("Details", "oi").invokeTab("Strings").invokeVisibility(v -> custom.getValue());

    public StringSetting state = setting("State", "by lyric").invokeTab("Strings").invokeVisibility(v -> custom.getValue());

    public ModeSetting mode = setting("Mode", "Dog", Arrays.asList("Cat", "Gun", "WDingz", "MioChair", "Dog", "Patrick", "FreakBob")).invokeTab("Image");

    public RichPresence()
    {
        super("RichPresence", Category.Miscellaneous);
    }

    @Override
    public void onEnable()
    {
        Managers.DISCORD.init();
    }

    @Override
    public void onDisable()
    {
        Managers.DISCORD.shutdown();
    }

    public String getDetails()
    {
        if (custom.getValue()) return detail.getValue();
        else return "version " + Skyfall.VERSION;
    }

    public String getState()
    {
        if (!custom.getValue())
        {
            String currentSong = Spotify.getCurrentSong();
            if (currentSong != null && !currentSong.isEmpty() && !currentSong.equals("Spotify") && !currentSong.equals("TIDAL"))
            {
                String[] parts = currentSong.split(" - ", 2);
                if (parts.length == 2)
                {
                    String artist = parts[0];
                    String track = parts[1].startsWith(" - ") ? parts[1].substring(3) : parts[1];
                    return "listening to " + track + " by " + artist;
                }
            }
            else {
                return "by lyric";
            }
        }
        return state.getValue();
    }

    public String getModeKey()
    {
        switch (mode.getValue())
        {
            case "Cat":
                return "cat";
            case "Gun":
                return "gun";
            case "MioChair":
                return "miochair";
                case "Dog":
                return "dog";
                case "WDingz":
                return "wdingz";
                case "Patrick":
                return "patrick";
                case "FreakBob":
                return "freakbob";
        }
        throw new RuntimeException("Invalid Mode Key: " + mode.getValue());
    }

    @Override
    public String displayAppend()
    {
        if (Managers.DISCORD.started) {
            return " connected";
        }
        else return " disconnected";
    }
}
