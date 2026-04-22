package me.lyric.skyfall.api.command;

import lombok.Getter;
import me.lyric.skyfall.api.utils.interfaces.Globals;

@Getter
public abstract class Command implements Globals {
    protected CommandInfo commandInfo;

    public Command() {
        if (this.getClass().isAnnotationPresent(CommandInfo.class)) {
            this.commandInfo = this.getClass().getAnnotation(CommandInfo.class);
        } else {
            throw new RuntimeException("CommandInfo annotation has not been found on " + this.getClass().getSimpleName());
        }
    }

    public abstract void onCommand(final String command, final String[] args);
}

