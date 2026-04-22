package me.lyric.skyfall.impl.event.mc;

import lombok.Getter;
import me.lyric.skyfall.api.event.Event;

@Getter
public class ScrollEvent extends Event {
    private final int mouseX, mouseY, amount;

    public ScrollEvent(int mouseX, int mouseY, int amount) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.amount = amount;
    }
}
