package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ActionSetting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.render.RenderUtils;

import java.awt.*;

public class ActionButton extends SettingDrawable {
    private final ActionSetting setting;

    public ActionButton(ActionSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;
        String text = setting.buttonName;

        Managers.TEXT.guiString(setting.getName(), x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        RenderUtils.rounded(x + width - (10.0f + Managers.TEXT.guiStringWidthSmall(text)), y + 2.0f, x + width, y + height - 2.0f, 5.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width - (10.0f + Managers.TEXT.guiStringWidthSmall(text)), y + 2.0f, x + width, y + height - 2.0f, 5.0f, FeatureTab.shade(-2));

        Color color = insideButton(mouseX, mouseY) ? Interface.primary() : Color.WHITE;
        Managers.TEXT.guiStringSmall(text, x + width - 5f - Managers.TEXT.guiStringWidthSmall(text), (y + height / 2.0f - Managers.TEXT.guiStringHeightSmall() / 2.0f) + 1f, color);

        height = 15.0f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && insideButton(mouseX, mouseY)) {
            setting.run();
        }
    }

    private boolean insideButton(int mouseX, int mouseY) {
        return mouseX > x + width - (10.0f + Managers.TEXT.guiStringWidthSmall(setting.buttonName) * 0.9) && mouseX < x + width && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
    }
}