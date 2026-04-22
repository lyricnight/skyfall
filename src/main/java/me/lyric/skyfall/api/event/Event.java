package me.lyric.skyfall.api.event;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lyric
 * global event class
 */

public class Event {
    @Getter
    @Setter
    private boolean cancelled;

    @Getter
    @Setter
    private boolean handled;
}
