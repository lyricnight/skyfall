package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;


import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;

import java.awt.*;

public class BooleanButton extends SettingDrawable {
    private final BooleanSetting setting;
    private float alpha, c;

    public BooleanButton(BooleanSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;
        Managers.TEXT.guiString(setting.getName(), x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, alpha));

        RenderUtils.rounded(x + width - 25.0f, y + 2.5f, x + width, y + height - 2.5f, 5.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width - 25.0f, y + 2.5f, x + width, y + height - 2.5f, 5.0f, FeatureTab.shade(-2));

        alpha = MathsUtils.lerp(alpha, setting.getValue() ? insideEnabled(mouseX, mouseY) ? 0.7f : 1.0f : insideEnabled(mouseX, mouseY) ? 0.7f : 0.4f, Interface.getDelta());
        c = MathsUtils.lerp(c, setting.getValue() ? 1.0f : 0.0f, Interface.getDelta());
        Color p = Interface.primary();
        Color color = new Color(p.getRed() / 255.0f, p.getGreen() / 255.0f, p.getBlue() / 255.0f, alpha);
        RenderUtils.circle(x + width - 21.0f + (13.0f * c), y + height / 2.0f - 2.0f, 4.0f, color);
        height = 15.0f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && insideEnabled(mouseX, mouseY)) {
            setting.invokeValue(!setting.getValue());
            if ("Drawn".equals(setting.getName())) {
                Managers.FEATURES.invalidateDrawableCache();
            }
        }
    }

    public boolean insideEnabled(int mouseX, int mouseY) {
        return mouseX > x + width - 25.0f && mouseX < x + width && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
    }
}
