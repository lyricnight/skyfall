package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.util.function.Predicate;

public class IntegerSetting extends Setting<Integer> {
    public int min, max;

    public IntegerSetting(final String name, final Integer value, int min, int max) {
        super(name, value);
        this.min = min;
        this.max = max;
    }

    @Override
    public IntegerSetting invokeVisibility(Predicate<Integer> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public IntegerSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}
