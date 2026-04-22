package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BindSetting;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class BindButton extends SettingDrawable {
    private final BindSetting setting;
    private boolean typing;

    public BindButton(BindSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;
        Managers.TEXT.guiString(setting.getName(), x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        RenderUtils.rounded(x + width - 50.0f, y + 2.0f, x + width, y + height - 2.0f, 5.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width - 50.0f, y + 2.0f, x + width, y + height - 2.0f, 5.0f, FeatureTab.shade(-2));

        String text = typing ? dots() : setting.getValue() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(setting.getValue());
        Managers.TEXT.guiStringSmall(text, (x + width - 25.0f - Managers.TEXT.guiStringWidthSmall(text) / 2.0f), (y + height / 2.0f - Managers.TEXT.guiStringHeightSmall() / 2.0f), Color.WHITE);
        height = 15.0f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && insideKey(mouseX, mouseY)) {
            typing = !typing;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (typing) {
            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_ESCAPE) {
                setting.invokeValue(Keyboard.KEY_NONE);
                typing = false;
            } else if (keyCode == Keyboard.KEY_RETURN) {
                typing = false;
            } else {
                setting.invokeValue(keyCode);
                typing = false;
            }
        }
    }

    private boolean insideKey(int mouseX, int mouseY) {
        return mouseX > x + width - 40.0f && mouseX < x + width && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
    }

    private long sys = 0L;

    private String dots() {
        float diff = System.currentTimeMillis() - sys;
        if (diff > 1333) {
            sys = System.currentTimeMillis();
            return "...";
        }
        if (diff > 999) {
            return "...";
        }
        if (diff > 666) {
            return "..";
        }
        if (diff > 333) {
            return ".";
        }
        return "";
    }
}
