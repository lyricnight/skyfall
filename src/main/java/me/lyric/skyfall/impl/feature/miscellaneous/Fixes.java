package me.lyric.skyfall.impl.feature.miscellaneous;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.BooleanSetting;

public final class Fixes extends Feature {
    public BooleanSetting sand = setting("Sand Render Check", false).invokeTab("Misc");

    public BooleanSetting enderman = setting("Enderman Teleport Check", false).invokeTab("Misc");

    public Fixes()
    {
        super("Fixes", Category.Miscellaneous);
    }
}
