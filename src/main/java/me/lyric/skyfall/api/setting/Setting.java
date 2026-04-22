package me.lyric.skyfall.api.setting;

import lombok.Getter;

import java.util.function.Predicate;

public abstract class Setting<T> {
    @Getter
    public final String name;
    @Getter
    public T value;
    protected Predicate<T> visible;
    @Getter
    public String tab = "Main";

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public void invokeValue(final T value) {
        this.value = value;
    }

    public Setting<T> invokeVisibility(final Predicate<T> visible) {
        this.visible = visible;
        return this;
    }

    public Setting<T> invokeTab(final String tab) {
        this.tab = tab;
        return this;
    }

    public boolean visible() {
        return visible == null || visible.test(this.value);
    }
}
