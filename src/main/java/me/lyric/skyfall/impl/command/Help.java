package me.lyric.skyfall.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.lyric.skyfall.api.command.Command;
import me.lyric.skyfall.api.command.CommandInfo;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.manager.Commands;

import java.util.StringJoiner;

@CommandInfo(name = "Help", description = "A help command for the mod", syntax = "help", aliases = {"help", "h"})
public class Help extends Command {
    @Override
    public void onCommand(String command, String[] args) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Command cmd : Commands.COMMANDS)
        {
            joiner.add(cmd.getCommandInfo().aliases()[0]);
        }
        Managers.MESSAGES.send(ChatFormatting.GREEN + String.format("Commands (%s) %s", Commands.COMMANDS.length, joiner));
    }
}
