package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.render.font.CustomFont;
import me.lyric.skyfall.impl.feature.internals.Fonts;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Text implements Globals {
    private final Map<String, Map<Integer, CustomFont>> fontCache = new HashMap<>();
    private String lastFont;

    public CustomFont getCustomFont(int size) {
        Fonts fontsFeature = Managers.FEATURES.get(Fonts.class);
        String fontName = fontsFeature.fontSelection.getValue();
        if (!Objects.equals(lastFont, fontName))
        {
            Managers.FEATURES.get(Fonts.class).onFontChanged();
            lastFont = fontName;
        }
        return getFont(fontName, size);
    }


    public CustomFont getFont(String fontName, int size) {
        return fontCache
            .computeIfAbsent(fontName, k -> new HashMap<>())
            .computeIfAbsent(size, s -> {
                try {
                    return createStaticFont(fontName + ".ttf", s);
                } catch (Exception e) {
                    Skyfall.LOGGER.warn("Failed to load font '{}', falling back to ProductSans", fontName, e);
                    try {
                        return createStaticFont("ProductSans.ttf", s);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to load fallback font ProductSans", ex);
                    }
                }
            });
    }

    public float stringHeight() {
        return getCustomFont(18).getHeight();
    }

    public float stringHeight(CustomFont font) {
        return font.getHeight();
    }

    public float stringWidth(String text) {
        return getCustomFont(18).getStringWidth(text);
    }

    public void string(String text, float x, float y, Color color) {
        getCustomFont(18).drawStringWithShadow(text, x, y, color);
    }

    public void string(String text, float x, float y, Color color, int size) {
        getCustomFont(size).drawStringWithShadow(text, x, y, color);
    }

    public void string(String text, float x, float y, Color color, CustomFont font) {
        font.drawStringWithShadow(text, x, y, color);
    }

    public void stringNoShadow(String text, float x, float y, Color color) {
        getCustomFont(18).drawString(text, x, y, color, false);
    }

    public void stringNoShadow(String text, float x, float y, Color color, int size) {
        getCustomFont(size).drawString(text, x, y, color, false);
    }

    public void stringNoShadow(String text, float x, float y, Color color, CustomFont font) {
        font.drawString(text, x, y, color, false);
    }

    public void guiStringVerySmall(String text, float x, float y, Color color) {
        getCustomFont(13).drawStringWithShadow(text, x, y, color);
        if (!Interface.search.isEmpty() && text.toLowerCase().contains(Interface.search.toLowerCase())){
            RenderUtils.rect(x, y , x + stringWidth(text), y + stringHeight() + 2.5f, new Color(Interface.primary().getRed(), Interface.primary().getGreen(), Interface.primary().getBlue(), 100));
        }
    }

    public void guiString(String text, float x, float y, Color color) {
        getCustomFont(18).drawStringWithShadow(text, x, y, color);
        if (!Interface.search.isEmpty() && text.toLowerCase().contains(Interface.search.toLowerCase())){
            RenderUtils.rect(x, y , x + stringWidth(text), y + stringHeight() + 2.5f, new Color(Interface.primary().getRed(), Interface.primary().getGreen(), Interface.primary().getBlue(), 100));
        }
    }

    public void guiStringLarge(String text, float x, float y, Color color) {
        getCustomFont(25).drawStringWithShadow(text, x, y, color);
        if (!Interface.search.isEmpty() && text.toLowerCase().contains(Interface.search.toLowerCase())){
            RenderUtils.rect(x, y , x + stringWidth(text), y + stringHeight() + 2.5f, new Color(Interface.primary().getRed(), Interface.primary().getGreen(), Interface.primary().getBlue(), 100));
        }
    }

    public void guiStringSmall(String text, float x, float y, Color color) {
        getCustomFont(15).drawStringWithShadow(text, x, y, color);
        if (!Interface.search.isEmpty() && text.toLowerCase().contains(Interface.search.toLowerCase())){
            RenderUtils.rect(x, y , x + stringWidth(text), y + stringHeight() + 2.5f, new Color(Interface.primary().getRed(), Interface.primary().getGreen(), Interface.primary().getBlue(), 100));
        }
    }

    @Deprecated
    public void hudString(String text, float x, float y, Color color, int size) {
        getCustomFont(size).drawStringWithShadow(text, x, y, color);
    }

    @Deprecated
    public float hudStringWidth(String text, int size) {
        return getCustomFont(size).getStringWidth(text);
    }

    public float guiStringWidthSmall(String text) {
        return getCustomFont(15).getStringWidth(text);
    }

    public float guiStringHeightSmall() {
        return getCustomFont(15).getHeight();
    }

    public float guiStringWidthVerySmall(String text) {
        return getCustomFont(13).getStringWidth(text);
    }

    public float guiStringHeightVerySmall() {
        return getCustomFont(13).getHeight();
    }

    public float guiStringHeightLarge() {
        return getCustomFont(25).getHeight();
    }


    public CustomFont createStaticFont(String name, int size) {
        try (InputStream resourceLocation = this.getClass().getResourceAsStream("/assets/minecraft/textures/font/" + name)) {
            if (resourceLocation == null) {
                throw new FileNotFoundException("Font resource not found: " + name);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, resourceLocation);
            Font derivedFont = font.deriveFont(Font.PLAIN, size);
            return new CustomFont(derivedFont);
        } catch (IOException | FontFormatException e) {
            Skyfall.LOGGER.error("Error loading static font: {}", name, e);
            try {
                throw e;
            } catch (IOException | FontFormatException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
