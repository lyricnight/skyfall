package me.lyric.skyfall.impl.event.mc;

import me.lyric.skyfall.api.event.Event;

/**
 * @author eva
 */
public class ClickEvent extends Event {
    public static class LeftClick extends ClickEvent {
        public LeftClick() {}
    }

    public static class RightClick extends ClickEvent {
        public RightClick() {}
    }

    public static class MiddleClick extends ClickEvent {
        public MiddleClick() {}
    }
}
