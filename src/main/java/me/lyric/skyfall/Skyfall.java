package me.lyric.skyfall;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.utils.spotify.AudioVisualizer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author lyric
 * main class
 */

@Mod(modid = Skyfall.MOD_ID, version = Skyfall.VERSION)
public final class Skyfall {
    public static final String MOD_ID = "skyfall";
    public static final String NAME = "Skyfall";
    public static final String VERSION = "2.0";
    public static Interface INTERFACE;
    private static long startTime = 0L;
    public static long endTime = 0L;
    public static boolean errored = false;
    /**
     * here, we use our own logger as we don't want to use <code> event.getModLog() </code> anyway.
     */
    public static final Logger LOGGER = LogManager.getLogger("Skyfall");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER.info("---------Pre-Init---------");
        startTime = System.nanoTime() / 1000000L;
        Managers.sub();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("---------Post-Init---------");
        Managers.init();
        INTERFACE = new Interface();
        endTime = System.nanoTime() / 1000000L - startTime;
        if (errored) {
            LOGGER.info("---------Skyfall has initialised with errors in {} ms--------", endTime);
        }
        else {
            LOGGER.info("---------Skyfall has fully initialised in {} ms--------", endTime);
        }
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {
        Skyfall.LOGGER.info("------Load Complete Event------");
        Managers.SPOTIFY.init();
        Managers.THREADS.scheduleDelayed(
                () -> {
                    if (errored)
                    {
                        Managers.NOTIFICATIONS.notify("Client Loaded With Errors!", "Skyfall loaded with errors in " + Skyfall.endTime + " ms",15000, "other/excl.png");
                    }
                    else
                    {
                        Managers.NOTIFICATIONS.notify("Client Loaded!", "Skyfall loaded completely in " + Skyfall.endTime + " ms",15000, "other/logo.png");
                    }
                    if (AudioVisualizer.audioInitialized)
                    {
                        Managers.NOTIFICATIONS.notify("Audio Capture Successful!", "Visualiser will display accurate audio.",15000, "other/reload.png");
                    }
                    else {
                        Managers.NOTIFICATIONS.notify("Audio Capture Failed!", "Visualiser will run in SIM mode",15000, "other/excl.png");
                    }
                }
                , 2, TimeUnit.SECONDS
        );
        Managers.FEATURES.onInit();
    }
}