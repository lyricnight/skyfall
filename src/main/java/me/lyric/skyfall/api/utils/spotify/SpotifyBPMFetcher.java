package me.lyric.skyfall.api.utils.spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.impl.hud.Spotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fetches song BPM (tries and fails most of the time)
 * @author lockedin
 * you need your own API key
 */
public class SpotifyBPMFetcher {
    private static final String GETSONGBPM_SEARCH_URL = "https://api.getsong.co/search/";
    private static final String GETSONGBPM_SONG_URL = "https://api.getsong.co/song/";
    private static final String API_KEY = "";
    private static final Map<String, CachedBPM> bpmCache = new HashMap<>();
    private static final long CACHE_DURATION = 3600000; // 1 hour

    private static class CachedBPM {
        float bpm;
        long timestamp;

        CachedBPM(float bpm) {
            this.bpm = bpm;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }

    /**
     * tempo fetcher using getsongbpm.com api - pretty shit since it doesn't have a lot of songs
     * @param trackName The name of the track
     * @param artistName The name of the artist
     * @return CompletableFuture with Float BPM or null if not found
     */
    public static CompletableFuture<Float> fetchBPM(String trackName, String artistName) {
        String cacheKey = trackName + ":" + artistName;
        Skyfall.LOGGER.info("[SpotifyBPM] Fetching BPM for: {} by {}", trackName, artistName);

        synchronized (bpmCache) {
            CachedBPM cached = bpmCache.get(cacheKey);
            if (cached != null) {
                if (!cached.isExpired()) {
                    Skyfall.LOGGER.info("[SpotifyBPM] Returning cached BPM ({}) for: {}", cached.bpm, trackName);
                    return CompletableFuture.completedFuture(cached.bpm);
                } else {
                    bpmCache.remove(cacheKey);
                    Skyfall.LOGGER.info("[SpotifyBPM] Removed expired BPM cache entry for: {}", trackName);
                }
            }
        }

        return Managers.THREADS.supplyAsync(() -> {
            try {
                String lookupQuery = "song:" + trackName + " artist:" + artistName;
                String encodedLookup = URLEncoder.encode(lookupQuery, "UTF-8");
                String searchUrl = GETSONGBPM_SEARCH_URL + "?api_key=" + API_KEY + "&type=both&lookup=" + encodedLookup;

                URL url = new URL(searchUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "SkyfallMod/1.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Skyfall.LOGGER.warn("[SpotifyBPM] GetSongBPM search returned code: {}", responseCode);
                    connection.disconnect();
                    return fallbackBPM();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                JsonObject searchResult = new JsonParser().parse(response.toString()).getAsJsonObject();
                if (!searchResult.has("search")) {
                    Skyfall.LOGGER.info("[SpotifyBPM] No search results for: {} by {}", trackName, artistName);
                    return fallbackBPM();
                }

                if (searchResult.get("search").isJsonObject()) {
                    JsonObject searchObj = searchResult.getAsJsonObject("search");
                    if (searchObj.has("error")) {
                        String error = searchObj.get("error").getAsString();
                        Skyfall.LOGGER.info("[SpotifyBPM] GetSongBPM returned error: '{}' for: {} by {}", error, trackName, artistName);
                        return fallbackBPM();
                    }
                }

                JsonArray tracks;
                if (searchResult.get("search").isJsonArray()) {
                    tracks = searchResult.getAsJsonArray("search");
                } else {
                    Skyfall.LOGGER.info("[SpotifyBPM] Unexpected search response format for: {} by {}", trackName, artistName);
                    return fallbackBPM();
                }

                if (tracks.size() == 0) {
                    Skyfall.LOGGER.info("[SpotifyBPM] No tracks found for: {} by {}", trackName, artistName);
                    return fallbackBPM();
                }
                JsonObject firstResult = tracks.get(0).getAsJsonObject();
                if (!firstResult.has("id")) {
                    Skyfall.LOGGER.warn("[SpotifyBPM] Search result missing ID field for: {}", trackName);
                    return fallbackBPM();
                }

                String songId = firstResult.get("id").getAsString();
                String songUrl = GETSONGBPM_SONG_URL + "?api_key=" + API_KEY + "&id=" + songId;

                url = new URL(songUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "SkyfallMod/1.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Skyfall.LOGGER.warn("[SpotifyBPM] GetSongBPM song endpoint returned code: {}", responseCode);
                    connection.disconnect();
                    return fallbackBPM();
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                JsonObject songResult = new JsonParser().parse(response.toString()).getAsJsonObject();
                if (!songResult.has("song")) {
                    Skyfall.LOGGER.warn("[SpotifyBPM] Song endpoint response missing 'song' field");
                    return fallbackBPM();
                }
                JsonObject song = songResult.getAsJsonObject("song");
                if (!song.has("tempo") || song.get("tempo").isJsonNull()) {
                    Skyfall.LOGGER.info("[SpotifyBPM] Song found but no BPM data for: {}", trackName);
                    return fallbackBPM();
                }

                String tempoStr = song.get("tempo").getAsString();
                float bpm = Float.parseFloat(tempoStr);

                if (bpm < 40 || bpm > 240) {
                    Skyfall.LOGGER.warn("[SpotifyBPM] Invalid BPM {} for: {}, using estimation", bpm, trackName);
                    return fallbackBPM();
                }

                synchronized (bpmCache) {
                    bpmCache.put(cacheKey, new CachedBPM(bpm));
                    Skyfall.LOGGER.info("[SpotifyBPM] Cached BPM for: {} (cache size: {})", trackName, bpmCache.size());
                }

                return bpm;
            } catch (Exception e) {
                Skyfall.LOGGER.error("[SpotifyBPM] Failed to fetch BPM for {}: {}", trackName, e.getMessage());
                ExceptionHandler.handle(e);
                return fallbackBPM();
            }
        });
    }

    /**
     * returns value of spotify setting
     * @return BPM
     */
    private static Float fallbackBPM() {
        return Managers.HUD.get(Spotify.class).bpm.getValue().floatValue();
    }

    /**
     * Clears the BPM cache
     */
    public static void clearCache() {
        synchronized (bpmCache) {
            int size = bpmCache.size();
            bpmCache.clear();
            Skyfall.LOGGER.info("[SpotifyBPM] Cleared BPM cache ({} entries)", size);
        }
    }

    /**
     * Removes expired entries from the cache
     * @return Number of entries removed
     */
    public static int cleanExpiredCache() {
        synchronized (bpmCache) {
            int initialSize = bpmCache.size();
            bpmCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            int removed = initialSize - bpmCache.size();
            if (removed > 0) {
                Skyfall.LOGGER.debug("[SpotifyBPM] Cleaned {} expired BPM cache entries", removed);
            }
            return removed;
        }
    }
}


