package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.util.function.Predicate;

public class FloatSetting extends Setting<Float> {
    public float min, max;

    public FloatSetting(String name, Float value, float min, float max) {
        super(name, value);
        this.min = min;
        this.max = max;
    }

    @Override
    public FloatSetting invokeVisibility(Predicate<Float> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public FloatSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}
