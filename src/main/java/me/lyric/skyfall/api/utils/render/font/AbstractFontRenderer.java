package me.lyric.skyfall.api.utils.render.font;

import java.awt.*;

public interface AbstractFontRenderer {
    float getStringWidth(String text);
    int drawStringWithShadow(String name, float x, float y, Color color);
    int drawStringGradient(String name, float x, float y, Color color, Color secondColor);
    int drawCenteredString(String name, float x, float y, Color color);
    String trimStringToWidth(String text, int width);
    String trimStringToWidth(String text, int width, boolean reverse);
    int drawString(String text, float x, float y, Color color, boolean shadow);
    int drawString(String name, float x, float y, Color color);
    float getMiddleOfBox(float height);
    int getHeight();
}