package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.api.utils.render.image.GifConverter;
import me.lyric.skyfall.api.utils.render.image.GifImage;
import me.lyric.skyfall.api.utils.render.image.ImageUtils;
import me.lyric.skyfall.api.utils.render.image.NamedImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyric
 * Manages all user-provided images - currently used in @link me.lyric.skyfall.impl.feature.render.HandShader
 */
public final class Images implements Globals {
    private static final File IMAGES = new File(Managers.CONFIG.getFolder() + File.separator + "images");
    private final Map<String, NamedImage> images = new ConcurrentHashMap<>();
    private final Map<String, GifImage> gifs = new ConcurrentHashMap<>();

    public void init()
    {
        if (!IMAGES.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            IMAGES.mkdir();
        }
        handleImages(IMAGES);
        for (File file : Objects.requireNonNull(IMAGES.listFiles()))
        {
            if (file.isDirectory())
            {
                handleImages(file);
            }
        }
    }

    private void handleImages(File dir)
    {
        if (dir.isDirectory())
        {
            for (File file : Objects.requireNonNull(
                    dir.listFiles((dir1, name) -> name.endsWith("gif") ||
                            name.endsWith("png")
                            || name.endsWith("jpg")
                            || name.endsWith("jpeg"))))
            {
                if (file.getName().endsWith("gif"))
                {
                    if (!gifs.containsKey(file.getName()))
                    {
                        try
                        {
                            if (!Null.is())
                            {
                                Managers.MESSAGES.send("Loading gif image: " + file.getName());
                            }
                            GifImage gif = GifConverter.readGifImage(Files.newInputStream(file.toPath()), file.getName());
                            gifs.put(file.getName(), gif);
                        }
                        catch (IOException e)
                        {
                            Skyfall.LOGGER.error("Failed to load gif image {}!", file.getName());
                            ExceptionHandler.handle(e);
                        }
                    }
                }
                else {
                    try
                    {
                        if (!images.containsKey(file.getName()))
                        {
                            String[] split = file.getName().split("\\.");
                            String format = split[split.length - 1];
                            NamedImage image = new NamedImage(ImageUtils.cacheBufferedImage(ImageUtils.createFlipped(ImageUtils.bufferedImageFromFile(file)), format, file.getName()), file.getName());
                            if (!Null.is())
                            {
                                Managers.MESSAGES.send("Loading image: " + file.getName());
                            }
                            images.put(file.getName(), image);
                        }
                    }
                    catch (IOException | NoSuchAlgorithmException e)
                    {
                        Skyfall.LOGGER.error("Failed to load image {}!", file.getName());
                        ExceptionHandler.handle(e);
                    }
                }
            }
        }
    }

    public NamedImage getImageByName(String name)
    {
        return images.get(name);
    }

    public GifImage getGifByName(String name)
    {
        return gifs.get(name);
    }
}
