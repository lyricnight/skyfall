package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.api.command.Command;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.impl.command.Help;
import me.lyric.skyfall.impl.command.Toggle;
import me.lyric.skyfall.impl.event.network.PacketEvent;
import me.lyric.skyfall.impl.feature.internals.Interface;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.Objects;

/**
 * @author evatron
 * TODO rewrite this completely
 */
public final class Commands implements Globals {

    public static Command[] COMMANDS;

    static {
        COMMANDS = new Command[]{
                new Help(),
                new Toggle(),
        };
        for (Command command : COMMANDS) {
            EventBus.getInstance().register(command);
            MinecraftForge.EVENT_BUS.register(command);
        }
    }

    public void callCommand(String input) {
        String[] spit = input.split(" ");
        String command = spit[0];
        String args = input.substring(command.length()).trim();

        for (Command c : COMMANDS) {
            for (String alias : c.getCommandInfo().aliases()) {
                if (alias.equalsIgnoreCase(command)) {
                    try {
                        c.onCommand(args, args.split(" "));
                    } catch (Exception e) {
                        Managers.MESSAGES.send("Internal error with command.");
                        ExceptionHandler.handle(e, e.getClass());
                    }
                    return;
                }
            }
        }

        Managers.MESSAGES.send("Command " + command.toLowerCase() + " doesn't exist");
    }

    @ITheAnnotation
    public void onPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof C01PacketChatMessage) {
            String message = ((C01PacketChatMessage) event.getPacket()).getMessage().substring(1);
            boolean shouldReturn = true;
            if (Objects.equals(Managers.FEATURES.get(Interface.class).prefix.value, "/")) {
                for (Command command : COMMANDS) {
                    for (String alias : command.getCommandInfo().aliases()) {
                        final String[] spit = message.split(" ");
                        final String name = spit[0];
                        if (alias.equalsIgnoreCase(name)) {
                            shouldReturn = false;
                            break;
                        }
                    }
                }
            } else {
                shouldReturn = false;
            }
            if (shouldReturn) return;
            if (((C01PacketChatMessage) event.getPacket()).getMessage().startsWith(Managers.FEATURES.get(Interface.class).prefix.value)) {
                callCommand(message);
                event.setCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (!Objects.equals(Managers.FEATURES.get(Interface.class).prefix.value, "/") && String.valueOf(Keyboard.getEventCharacter()).equals(Managers.FEATURES.get(Interface.class).prefix.value)) {
            mc.displayGuiScreen(new GuiChat(Managers.FEATURES.get(Interface.class).prefix.value));
        }
    }
}