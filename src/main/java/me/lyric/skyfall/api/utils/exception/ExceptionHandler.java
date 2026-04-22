package me.lyric.skyfall.api.utils.exception;

import com.mojang.realmsclient.gui.ChatFormatting;
import lombok.experimental.UtilityClass;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.event.Event;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.feature.internals.Interface;

/**
 * @author lyric
 * handles exceptions.
 */
@UtilityClass
public class ExceptionHandler {

    /**
     * handles generic exceptions.
     * @param t - the throwable to handle
     */
    public static void handle(Throwable t)
    {
        Skyfall.LOGGER.catching(t);
        Skyfall.errored = true;
        if (Null.is()) return;
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        Managers.MESSAGES.send(ChatFormatting.RED + "Skyfall caught and logged an exception!");
        Managers.MESSAGES.send(ChatFormatting.RED + "Type: " + t.getClass().getSimpleName());
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        if (Managers.FEATURES.get(Interface.class).notify.getValue())
        {
            Managers.NOTIFICATIONS.notify("Caught Exception!", "Caught exception of type: " + t.getClass().getSimpleName(), 5000, "other/excl.png");
        }
    }

    /**
     * handles exceptions in a specific class.
     * @param t - the throwable to handle
     * @param c - the class to handle
     */
    public static void handle(Throwable t, Class<?> c)
    {
        Skyfall.LOGGER.catching(t);
        Skyfall.errored = true;
        if (Null.is()) return;
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        Managers.MESSAGES.send(ChatFormatting.RED + "Skyfall caught and logged an exception in a feature/manager!");
        Managers.MESSAGES.send(ChatFormatting.RED + "Type: " + t.getClass().getSimpleName());
        Managers.MESSAGES.send(ChatFormatting.RED + "Class: " + c.getSimpleName());
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        if (Managers.FEATURES.get(Interface.class).notify.getValue())
        {
            Managers.NOTIFICATIONS.notify("Caught Exception!", "Caught exception of type: " + t.getClass().getSimpleName() + " in " + c.getSimpleName(), 5000, "other/excl.png");
        }
    }

    /**
     * handles exceptions in the event bus.
     * @param t - the throwable to handle
     * @param event - the event to handle
     */
    public static void handleEventFailure(Throwable t, Event event)
    {
        Skyfall.LOGGER.catching(t);
        Skyfall.errored = true;
        if (Null.is()) return;
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        Managers.MESSAGES.send(ChatFormatting.RED + "Skyfall's EventBus FAILED to fire an event!");
        Managers.MESSAGES.send(ChatFormatting.RED + "Event: " + event.getClass().getSimpleName());
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        if (Managers.FEATURES.get(Interface.class).notify.getValue())
        {
            Managers.NOTIFICATIONS.notify("Failed to fire event!", "Event: " + event.getClass().getSimpleName(), 7500, "other/excl.png");
        }
    }

    public static void handleTaskFailure(Throwable t)
    {
        Skyfall.LOGGER.catching(t);
        Skyfall.errored = true;
        if (Null.is()) return;
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        Managers.MESSAGES.send(ChatFormatting.RED + "Skyfall's Task Scheduler failed to run a task!");
        Managers.MESSAGES.send(ChatFormatting.RED + "------------------------------------");
        if (Managers.FEATURES.get(Interface.class).notify.getValue())
        {
            Managers.NOTIFICATIONS.notify("Failed to run task!", "Report this!", 7500, "other/excl.png");
        }
    }
}
