package me.lyric.skyfall.api.setting.types;

import me.lyric.skyfall.api.setting.Setting;

import java.util.function.Predicate;

public class ActionSetting extends Setting<Runnable> {
    public final Runnable value;
    public final String buttonName;

    public ActionSetting(String name, String buttonName, Runnable value) {
        super(name, value);
        this.value = value;
        this.buttonName = buttonName;
    }

    public void run() {
        value.run();
    }

    @Override
    public ActionSetting invokeVisibility(Predicate<Runnable> visible) {
        super.invokeVisibility(visible);
        return this;
    }

    @Override
    public ActionSetting invokeTab(String tab) {
        this.tab = tab;
        return this;
    }
}
