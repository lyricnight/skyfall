package me.lyric.skyfall.api.utils.render.image;

import lombok.Getter;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import org.lwjgl.Sys;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author 3arth
 */
public class GifImage {
    @Getter
    private final String name;
    private final List<BufferedImage> frames = new LinkedList<>();
    private final List<TextureStore> textures = new LinkedList<>();
    private int offset;
    private final int delay;
    private boolean firstUpdate;
    private long lastUpdate;
    private long timeLeft;

    public GifImage(List<BufferedImage> images, int delay, String name)
    {
        this.name = name;
        for (BufferedImage image : images)
        {
            this.frames.add(ImageUtils.createFlipped(image));
        }
        this.offset = 0;
        this.delay = delay;
        firstUpdate = true;
        for (BufferedImage image : this.frames)
        {
            try
            {
                String generatedString = UUID.randomUUID().toString().split("-")[0];
                textures.add(ImageUtils.cacheBufferedImage(image, "gif", generatedString));
            }
            catch (NoSuchAlgorithmException | IOException e)
            {
                ExceptionHandler.handle(e);
            }
        }
        reset();
    }

    public void reset() {
        firstUpdate = true;
        timeLeft = delay;
        offset = 0;
    }

    public BufferedImage getBufferedImage()
    {
        if (updateOffset()) {
            return null;
        }
        return frames.get(offset);
    }

    public TextureStore getDynamicTexture()
    {
        if (updateOffset()) {
            return null;
        }
        return textures.get(offset);
    }

    /**
     * @return true if {@code frames.size() == 0}
     */
    private boolean updateOffset()
    {
        if (frames.isEmpty()) return true;
        long now = getTime();
        long delta = now - lastUpdate;
        if (firstUpdate) {
            delta = 0;
            firstUpdate = false;
        }
        lastUpdate = now;
        timeLeft -= delta;
        if (timeLeft <= 0)
        {
            offset++;
            timeLeft = delay;
        }
        if (offset >= frames.size()) offset = 0;
        return false;
    }

    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public int getTextureSize() {
        return textures.size();
    }
}
