package me.lyric.skyfall.api.manager;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.impl.manager.*;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author lyric
 * typical manager loading class.
 */
public final class Managers {
    public static final Features FEATURES = new Features();
    public static final Discord DISCORD = new Discord();
    public static final Commands COMMANDS = new Commands();
    public static final Config CONFIG = new Config();
    public static final HUD HUD = new HUD();
    public static final Spotify SPOTIFY = new Spotify();
    public static final Threads THREADS = new Threads();
    public static final Text TEXT = new Text();
    public static final Server SERVER = new Server();
    public static final Messages MESSAGES = new Messages();
    public static final Player PLAYER = new Player();
    public static final Location LOCATION = new Location();
    public static final Notifications NOTIFICATIONS = new Notifications();
    public static final Clicker CLICKER = new Clicker();
    public static final Images IMAGES = new Images();
    public static final Shaders SHADERS = new Shaders();

    public static void sub()
    {
        Skyfall.LOGGER.info("Beginning subscription.");
        subscribe(DISCORD, FEATURES, COMMANDS, PLAYER, MESSAGES, HUD, SERVER, NOTIFICATIONS, LOCATION);
        Skyfall.LOGGER.info("Finished subscription.");
    }

    public static void init() {
        Skyfall.LOGGER.info("Starting init.");
        THREADS.init();
        IMAGES.init();
        FEATURES.init();
        HUD.init();
        CONFIG.init();
        SERVER.init();
        Skyfall.LOGGER.info("Finished init.");
    }

    public static void unload()
    {
        Skyfall.LOGGER.info("Started unload.");
        CONFIG.save("AutoSave", true, new ArrayList<>(Arrays.asList(Category.values())));
        SPOTIFY.shutdown();
        SHADERS.shutdown();
        DISCORD.shutdown();
        THREADS.shutdown();
        Skyfall.LOGGER.info("Finished unload.");
    }

    private static void subscribe(Object... subs)
    {
        for (Object sub : subs)
        {
            EventBus.getInstance().register(sub);
            MinecraftForge.EVENT_BUS.register(sub);
        }
    }
}
