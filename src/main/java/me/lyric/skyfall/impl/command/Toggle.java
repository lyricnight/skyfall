package me.lyric.skyfall.impl.command;

import me.lyric.skyfall.api.command.Command;
import me.lyric.skyfall.api.command.CommandInfo;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;

@CommandInfo(name = "Toggle", description = "A command for toggling features", syntax = ".toggle", aliases = {"toggle", "t", "hescheating"})
public class Toggle extends Command {

    @Override
    public void onCommand(final String command, final String[] args) {
        Managers.FEATURES.getFeatures().stream().filter(feature -> feature.getName().equalsIgnoreCase(args[0])).forEach(Feature::toggle);
    }
}
