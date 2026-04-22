package me.lyric.skyfall.api.utils.render.image;

import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.interfaces.ducks.ITextureManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public final class ImageUtils implements Globals {

    public static BufferedImage createFlipped(BufferedImage image)
    {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        return createTransformed(image, at);
    }

    public static BufferedImage createTransformed(BufferedImage image, AffineTransform at)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public static BufferedImage bufferedImageFromFile(File file) throws IOException
    {
        java.awt.Image image = ImageIO.read(file);
        String format = file.getName().split("\\.")[1];
        BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    @SuppressWarnings("RedundantThrows")
    public static TextureStore cacheBufferedImage(BufferedImage image, String format, String name) throws NoSuchAlgorithmException, IOException {
        TextureStore texture = new TextureStore(image);
        ResourceLocation location = ((ITextureManager) mc.getTextureManager()).skyfall$getTextureResourceLocation(name, texture);
        mc.getTextureManager().loadTexture(location, texture);
        return texture;
    }
}
