package me.lyric.skyfall.impl.feature.internals;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ModeSetting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lyric
 * controls font rendering system.
 */
public final class Fonts extends Feature {
    public ModeSetting fontSelection;

    public Fonts() {
        super("Fonts", Category.Internals);
        List<String> availableFonts = scanAvailableFonts();
        fontSelection = setting("Font", "ProductSans", availableFonts).invokeTab("Font");
        setEnabled(true);
    }

    /**
     * called when the font selection changes
     */
    public void onFontChanged() {
        Skyfall.LOGGER.info("Font changed, invalidating HUD font caches");
        for (HUDBase hud : Managers.HUD.getHudModules()) {
            hud.invalidateFontCache();
        }
    }

    @Override
    public void onDisable()
    {
        toggle();
        Managers.NOTIFICATIONS.notify("Fonts", "You can't turn off this feature.", 2000, "other/excl.png");
    }

    /**
     * Scans the font directory for available .ttf and .otf font files.
     * @return List of font names
     */
    private List<String> scanAvailableFonts() {
        List<String> fonts = new ArrayList<>();

        try {
            String fontPath = "/assets/minecraft/textures/font/";
            URL resource = getClass().getResource(fontPath);

            if (resource == null) {
                Skyfall.LOGGER.error("Font directory not found: {}", fontPath);
                return Collections.singletonList("ProductSans");
            }
            if (resource.getProtocol().equals("jar")) {
                fonts.addAll(scanFontsFromJar(fontPath));
            } else {
                fonts.addAll(scanFontsFromDirectory(resource));
            }

            fonts.sort(String.CASE_INSENSITIVE_ORDER);

            if (fonts.isEmpty()) {
                Skyfall.LOGGER.warn("No fonts found, using default ProductSans");
                fonts.add("ProductSans");
            }

            Skyfall.LOGGER.info("Found {} font(s): {}", fonts.size(), String.join(", ", fonts));

        } catch (Exception e) {
            Skyfall.LOGGER.error("Error scanning fonts directory", e);
            fonts.add("ProductSans");
        }

        return fonts;
    }

    /**
     * Scans fonts from jar
     */
    private List<String> scanFontsFromJar(String fontPath) throws IOException, URISyntaxException {
        List<String> fonts = new ArrayList<>();
        URL resourceUrl = getClass().getResource(fontPath);
        if (resourceUrl == null) {
            return fonts;
        }
        URI uri = resourceUrl.toURI();
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
            Path path = fileSystem.getPath(fontPath);

            try (Stream<Path> walk = Files.walk(path, 1)) {
                fonts = walk
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"))
                    .map(this::stripFontExtension)
                    .collect(Collectors.toList());
            }
        }
        return fonts;
    }

    /**
     * Scans fonts from a directory
     */
    private List<String> scanFontsFromDirectory(URL resource) throws URISyntaxException {
        List<String> fonts = new ArrayList<>();
        File directory = new File(resource.toURI());

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"));

            if (files != null) {
                for (File file : files) {
                    fonts.add(stripFontExtension(file.getName()));
                }
            }
        }

        return fonts;
    }

    /**
     * Removes the file extension from a font filename.
     */
    private String stripFontExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }

    @Override
    public void onInit() {
        validateFontSelection();
    }

    private void validateFontSelection() {
        String selectedFont = fontSelection.getValue();
        String fontPath = "/assets/minecraft/textures/font/" + selectedFont + ".ttf";
        try (InputStream is = getClass().getResourceAsStream(fontPath)) {
            if (is == null) {
                fontPath = "/assets/minecraft/textures/font/" + selectedFont + ".otf";
                try (InputStream is2 = getClass().getResourceAsStream(fontPath)) {
                    if (is2 == null) {
                        Skyfall.LOGGER.warn("Selected font '{}' not found, falling back to ProductSans", selectedFont);
                        fontSelection.invokeValue("ProductSans");
                    }
                }
            }
        } catch (IOException e) {
            Skyfall.LOGGER.error("Error validating font: {}", selectedFont, e);
            fontSelection.invokeValue("ProductSans");
        }
    }
}

