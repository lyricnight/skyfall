package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.util.function.Predicate;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name, Integer value) {
        super(name, value);
    }

    @Override
    public BindSetting invokeVisibility(Predicate<Integer> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public BindSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}

