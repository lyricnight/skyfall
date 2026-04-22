package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.awt.*;
import java.util.function.Predicate;

public class ColourSetting extends Setting<Color> {
    public ColourSetting(String name, Color value) {
        super(name, value);
    }

    @Override
    public ColourSetting invokeVisibility(Predicate<Color> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public ColourSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}
