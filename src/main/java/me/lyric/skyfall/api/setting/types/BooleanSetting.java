package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.util.function.Predicate;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, Boolean value) {
        super(name, value);
    }

    @Override
    public BooleanSetting invokeVisibility(Predicate<Boolean> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public BooleanSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}
