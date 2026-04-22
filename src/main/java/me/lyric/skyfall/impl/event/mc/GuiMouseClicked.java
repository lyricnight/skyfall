package me.lyric.skyfall.impl.event.mc;

import lombok.Getter;
import me.lyric.skyfall.api.event.Event;
import net.minecraft.client.gui.GuiScreen;

@Getter
public class GuiMouseClicked extends Event {
    private final int x, y, key;
    private final GuiScreen screen;

    public GuiMouseClicked(int x, int y, int key, GuiScreen screen) {
        this.x = x;
        this.y = y;
        this.key = key;
        this.screen = screen;
    }

}