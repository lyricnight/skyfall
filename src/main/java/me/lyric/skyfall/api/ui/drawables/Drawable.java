package me.lyric.skyfall.api.ui.drawables;

import me.lyric.skyfall.api.utils.interfaces.Globals;

public interface Drawable extends Globals {
    void drawScreen(int mouseX, int mouseY, float partialTicks);

    void mouseClicked(int mouseX, int mouseY, int mouseButton);

    void keyTyped(char typedChar, int keyCode);
}
