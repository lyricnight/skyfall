package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.StringSetting;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class StringButton extends SettingDrawable {
    private final StringSetting setting;
    private boolean typing = false;
    private int cursorOffset = 0;
    private long lastTime = System.currentTimeMillis();

    @SuppressWarnings("FieldCanBeLocal")
    private final String allowed = "1!2@3#4$5%6^7&8*9(0)-_=+qQwWeErRtTyYuUiIoOpP[{]}\\|aAsSdDfFgGhHjJkKlL;:'\"zZxXcCvVbBnNmM,<.>/?` ";

    public StringButton(StringSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;

        Managers.TEXT.guiString(setting.getName(), x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        String value = setting.getValue();
        float boxRight = x + width;
        float boxLeft = boxRight - (10.0f + Managers.TEXT.guiStringWidthSmall(value));
        float boxTop = y + 2.0f;
        float boxBottom = y + height - 2.0f;

        RenderUtils.rounded(boxLeft, boxTop, boxRight, boxBottom, 5.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(boxLeft, boxTop, boxRight, boxBottom, 5.0f, FeatureTab.shade(-2));

        float baseX = boxRight - 5f - Managers.TEXT.guiStringWidthSmall(value);
        float cursorX = baseX + Managers.TEXT.guiStringWidthSmall(value.substring(0, value.length() - cursorOffset));
        float textY = y + height / 2.0f - Managers.TEXT.guiStringHeightSmall() / 2.0f;

        Managers.TEXT.guiStringSmall(value, baseX, textY + 1f, Color.WHITE);
        if (typing && isCursorVisible()) {
            float cursorHeight = Managers.TEXT.guiStringHeightSmall();
            RenderUtils.rect(cursorX, textY - 2f, cursorX + 0.5f, textY - 2f + cursorHeight + 4f, Color.WHITE);
        }

        height = 15.0f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            typing = insideKey(mouseX, mouseY);
            if (typing) {
                lastTime = System.currentTimeMillis();
                cursorVisible = true;
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (typing) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
                cursorOffset = 0;
                typing = false;
            } else if ((keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) && !setting.getValue().isEmpty()) {
                int deleteIndex = setting.getValue().length() - 1 - cursorOffset;
                if (deleteIndex >= 0 && deleteIndex < setting.getValue().length()) {
                    setting.invokeValue(setting.getValue().substring(0, deleteIndex) + setting.getValue().substring(deleteIndex + 1));
                    lastTime = System.currentTimeMillis();
                    cursorVisible = true;
                }
            } else if (allowed.contains(String.valueOf(typedChar))) {
                int insertIndex = setting.getValue().length() - cursorOffset;
                setting.invokeValue(setting.getValue().substring(0, insertIndex) + typedChar + setting.getValue().substring(insertIndex));
                lastTime = System.currentTimeMillis();
                cursorVisible = true;
            } else if (keyCode == 203) { //< left arrow
                if (cursorOffset < setting.getValue().length() - 1) cursorOffset = cursorOffset + 1;
                lastTime = System.currentTimeMillis();
                cursorVisible = true;
            } else if (keyCode == 205) { //> right arrow
                if (cursorOffset > 0) cursorOffset = cursorOffset - 1;
                lastTime = System.currentTimeMillis();
                cursorVisible = true;
            }
        }
    }

    private boolean insideKey(int mouseX, int mouseY) {
        return mouseX > x + width - (10.0f + Managers.TEXT.guiStringWidthSmall(setting.getValue()) * 0.9) && mouseX < x + width && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
    }
    private boolean cursorVisible = true;

    private boolean isCursorVisible() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 500) {
            cursorVisible = !cursorVisible;
            lastTime = currentTime;
        }
        return cursorVisible;
    }
}
