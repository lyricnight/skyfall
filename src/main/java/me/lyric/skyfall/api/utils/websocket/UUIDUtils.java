package me.lyric.skyfall.api.utils.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

//TODO make this a manager when or if we actually use it

/**
 * currently unused, but kept for when needed.
 */
@UtilityClass
public final class UUIDUtils {
    private static final ConcurrentHashMap<String, String> uuidCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 1000 * 60 * 30;
    private static final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    /**
     * threaded uuid lookup
     * @param name The player name
     * @return CompletableFuture with the UUID
     */
    public static CompletableFuture<String> getUUIDAsync(String name) {
        // Check cache first
        String cached = uuidCache.get(name.toLowerCase());
        if (cached != null) {
            Long timestamp = cacheTimestamps.get(name.toLowerCase());
            if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_DURATION) {
                Skyfall.LOGGER.info("Returning cached UUID for {}", name);
                return CompletableFuture.completedFuture(cached);
            } else {
                uuidCache.remove(name.toLowerCase());
                cacheTimestamps.remove(name.toLowerCase());
            }
        }
        return Managers.THREADS.supplyAsync(() -> fetchUUID(name));
    }

    private static String fetchUUID(String name) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }

            String str = result.toString();
            if (str.isEmpty())
                return "";
            else {
                JsonObject convertedObject = new Gson().fromJson(str, JsonObject.class);
                JsonElement value = convertedObject.get("id");
                if (value.isJsonNull()) return "";
                else {
                    String uuid = value.getAsString();
                    uuidCache.put(name.toLowerCase(), uuid);
                    cacheTimestamps.put(name.toLowerCase(), System.currentTimeMillis());
                    Skyfall.LOGGER.info("UUID for {} is {}.", name, uuid);
                    return uuid;
                }
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            Skyfall.LOGGER.error("Failed to get UUID for {}: {}", name, e.getMessage());
            return "error";
        }
    }

    public static void clearCache() {
        uuidCache.clear();
        cacheTimestamps.clear();
        Skyfall.LOGGER.info("UUID cache cleared");
    }
}
