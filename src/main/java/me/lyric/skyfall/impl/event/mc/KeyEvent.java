package me.lyric.skyfall.impl.event.mc;

import lombok.Getter;
import me.lyric.skyfall.api.event.Event;

@Getter
public class KeyEvent extends Event {
    private final int key;

    public KeyEvent(int key) {
        this.key = key;
    }
}
