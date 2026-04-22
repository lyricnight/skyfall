package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.util.function.Predicate;

public class StringSetting extends Setting<String> {

    public final String value;

    public StringSetting(String name, String value) {
        super(name, value);
        this.value = value;
    }

    @Override
    public StringSetting invokeVisibility(Predicate<String> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public StringSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}
