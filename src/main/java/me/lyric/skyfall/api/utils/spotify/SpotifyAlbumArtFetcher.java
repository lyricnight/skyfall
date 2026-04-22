package me.lyric.skyfall.api.utils.spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author spotify dev person idfk
 * put your own API keys here for this to work
 */
public class SpotifyAlbumArtFetcher {
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String SEARCH_URL = "https://api.spotify.com/v1/search";

    private static String accessToken = null;
    private static long tokenExpiry = 0;
    private static final Map<String, CachedImage> imageCache = new HashMap<>();
    private static final long CACHE_DURATION = 500000;
    private static final long CLEANUP_INTERVAL = 600000; // 10 minutes
    //made this volatile
    private static volatile ScheduledFuture<?> cleanupTask = null;

    private static class CachedImage {
        BufferedImage image;
        long timestamp;

        CachedImage(BufferedImage image) {
            this.image = image;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }

    /**
     * Fetches album art asynchronously for the given track using our thread manager
     * @param trackName The name of the track
     * @param artistName The name of the artist
     * @return CompletableFuture with BufferedImage or null if not found
     */
    public static CompletableFuture<BufferedImage> fetchAlbumArt(String trackName, String artistName) {
        String cacheKey = trackName + ":" + artistName;
        Skyfall.LOGGER.info("Fetching album art for: {} by {}", trackName, artistName);

        synchronized (imageCache) {
            CachedImage cached = imageCache.get(cacheKey);
            if (cached != null) {
                if (!cached.isExpired()) {
                    Skyfall.LOGGER.info("Returning cached album art for: {}", trackName);
                    return CompletableFuture.completedFuture(cached.image);
                } else {
                    imageCache.remove(cacheKey);
                    Skyfall.LOGGER.info("Removed expired cache entry for: {}", trackName);
                }
            }
        }

        return Managers.THREADS.supplyAsync(() -> {
            try {
                if (accessToken == null || System.currentTimeMillis() >= tokenExpiry) {
                    Skyfall.LOGGER.info("Access token expired or null, refreshing...");
                    if (!refreshAccessToken()) {
                        Skyfall.LOGGER.error("Failed to refresh access token");
                        return null;
                    }
                    Skyfall.LOGGER.info("Access token refreshed successfully");
                }

                String query = URLEncoder.encode(trackName + " " + artistName, "UTF-8");
                String searchEndpoint = SEARCH_URL + "?q=" + query + "&type=track&limit=1";

                URL url = new URL(searchEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setConnectTimeout(10000); // 10 second timeout
                connection.setReadTimeout(10000); // 10 second timeout

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Skyfall.LOGGER.error("Spotify search failed with response code: {}", responseCode);
                    connection.disconnect();
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();

                String searchJson = response.toString();
                JsonObject searchResult = new JsonParser().parse(searchJson).getAsJsonObject();
                JsonArray tracks = searchResult.getAsJsonObject("tracks").getAsJsonArray("items");

                if (tracks.size() == 0) {
                    return null;
                }

                JsonObject track = tracks.get(0).getAsJsonObject();
                JsonObject album = track.getAsJsonObject("album");
                JsonArray images = album.getAsJsonArray("images");

                if (images.size() == 0) {
                    return null;
                }

                String imageUrl;
                if (images.size() >= 2) {
                    imageUrl = images.get(1).getAsJsonObject().get("url").getAsString();
                } else {
                    imageUrl = images.get(0).getAsJsonObject().get("url").getAsString();
                }
                BufferedImage image = ImageIO.read(new URL(imageUrl));

                if (image == null) {
                    return null;
                }

                synchronized (imageCache) {
                    imageCache.put(cacheKey, new CachedImage(image));
                    Skyfall.LOGGER.info("Cached album art for: {} (cache size: {})", trackName, imageCache.size());
                }

                return image;
            } catch (Exception e) {
                Skyfall.LOGGER.error("Failed to fetch album art for {}: {}", trackName, e.getMessage());
                ExceptionHandler.handle(e);
                return null;
            }
        });
    }

    /**
     * Refreshes the Spotify API access token
     */
    private static boolean refreshAccessToken() {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String auth = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "grant_type=client_credentials";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            String json = response.toString();

            if (json.contains("error")) {
                Skyfall.LOGGER.error("Spotify API Error: {}", json);
                return false;
            }

            JsonObject tokenResponse = new JsonParser().parse(json).getAsJsonObject();
            accessToken = tokenResponse.get("access_token").getAsString();
            int expiresIn = tokenResponse.get("expires_in").getAsInt();
            tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000L) - 60000;

            Skyfall.LOGGER.info("Spotify API token refreshed successfully");
            return true;
        } catch (Exception e) {
            Skyfall.LOGGER.error("Failed to refresh Spotify access token: {}", e.getMessage());
            ExceptionHandler.handle(e);
            return false;
        }
    }

    /**
     * Clears the entire image cache
     */
    public static void clearCache() {
        synchronized (imageCache) {
            int size = imageCache.size();
            imageCache.clear();
            Skyfall.LOGGER.info("Cleared album art cache ({} entries)", size);
        }
    }

    /**
     * Removes expired entries from the cache
     */
    public static int cleanExpiredCache() {
        synchronized (imageCache) {
            int initialSize = imageCache.size();
            imageCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            int removed = initialSize - imageCache.size();
            if (removed > 0) {
                Skyfall.LOGGER.debug("Cleaned {} expired album art cache entries", removed);
            }
            return removed;
        }
    }

    public static void initializeCleanupScheduler() {
        if (cleanupTask == null) {
            synchronized (SpotifyAlbumArtFetcher.class) {
                if (cleanupTask == null) {
                    cleanupTask = Managers.THREADS.scheduleRepeating(() -> {
                        try {
                            int removed = cleanExpiredCache();
                            if (removed > 0) {
                                Skyfall.LOGGER.info("Automatic cache cleanup removed {} expired entries", removed);
                            }
                            int bpmRemoved = SpotifyBPMFetcher.cleanExpiredCache();
                            if (bpmRemoved > 0) {
                                Skyfall.LOGGER.info("Automatic BPM cache cleanup removed {} expired entries", bpmRemoved);
                            }
                        } catch (Exception e) {
                            Skyfall.LOGGER.error("Error during automatic cache cleanup: {}", e.getMessage());
                        }
                    }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);

                    Skyfall.LOGGER.info("Automatic album art cache cleanup scheduler started (interval: {} minutes)", CLEANUP_INTERVAL / 60000);
                }
            }
        }
    }

    public static void shutdown() {
        if (cleanupTask != null && !cleanupTask.isCancelled()) {
            cleanupTask.cancel(false);
            Skyfall.LOGGER.info("Album art cleanup task cancelled");
        }
        clearCache();
        SpotifyBPMFetcher.clearCache();
    }
}

