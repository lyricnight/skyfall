package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.spotify.AudioVisualizer;
import me.lyric.skyfall.api.utils.spotify.SpotifyAlbumArtFetcher;

import java.util.concurrent.TimeUnit;

/**
 * @author lyric
 * this class is meant to act as a buffer between the spotify util classes and the rest of the client.
 */
public final class Spotify {

    public void init()
    {
        //this way we don't use the while (polling) pattern which is bad for performance
        //updates every 2000 millis (probably overkill tbh, 5 seconds makes more sense but requires testing)
        Managers.THREADS.runSpotifyPolling(() -> {
            try {
                me.lyric.skyfall.impl.hud.Spotify.getSpotify();
            } catch (InterruptedException e) {
                Skyfall.LOGGER.info("Spotify polling interrupted, shutting down");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Skyfall.LOGGER.error("Spotify polling error: {}", e.getMessage());
            }
        });
        AudioVisualizer.start();
        SpotifyAlbumArtFetcher.initializeCleanupScheduler();
    }

    public void shutdown()
    {
        AudioVisualizer.stop();
        me.lyric.skyfall.impl.hud.Spotify.stopPolling();
        SpotifyAlbumArtFetcher.shutdown();
    }
}
