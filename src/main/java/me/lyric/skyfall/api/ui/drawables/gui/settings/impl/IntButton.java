package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class IntButton extends SettingDrawable {
    private final IntegerSetting setting;
    private float sliderX;
    private boolean clickedSlider = false;

    public IntButton(IntegerSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;
        Managers.TEXT.guiString(setting.getName(), x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        RenderUtils.rect(x + width / 2.0f - 10.0f, y + 6.5f, x + width - 10.0f, y + height - 6.5f, FeatureTab.shade(5));

        float w = width / 2.0f,
                v = setting.value - setting.min,
                m = setting.max - setting.min,
                targetX = x + width / 2.0f - 10.0f + w * (v / m);
        sliderX = MathsUtils.lerp(sliderX, targetX, Interface.getDelta());

        RenderUtils.rect(x + width / 2.0f - 10.0f, y + 6.5f, sliderX, y + height - 6.5f, Interface.primary());
        RenderUtils.roundedOutline(x + width / 2.0f - 10.0f, y + 6.5f, x + width - 10.0f, y + height - 6.5f, 0.0f, FeatureTab.shade(-3));

        RenderUtils.circle(sliderX - 1.75f, y + height / 2.0f - 1.75f, 3.5f, FeatureTab.shade(-3));
        RenderUtils.circle(sliderX - 1.5f, y + height / 2.0f - 1.5f, 3.0f, Interface.primary());

        String value = setting.getValue().toString();
        Managers.TEXT.guiStringVerySmall(value, sliderX - Managers.TEXT.guiStringWidthVerySmall(value) / 2.0f, y - 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        value = String.valueOf(setting.min);
        Managers.TEXT.guiStringVerySmall(value, x + width / 2.0f - 12.5f - Managers.TEXT.guiStringWidthVerySmall(value), y + height / 2.0f - Managers.TEXT.guiStringHeightVerySmall() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        value = String.valueOf(setting.max);
        Managers.TEXT.guiStringVerySmall(value, x + width - 7.5f, y + height / 2.0f - Managers.TEXT.guiStringHeightVerySmall() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        if ((DefaultScreen.getActiveFeature() != null || Interface.selectedScreen.equals("HudEditor")) && visibleAnim > 0.9f) {
            if (!Mouse.isButtonDown(0)) {
                clickedSlider = false;
            }
            if (clickedSlider) {
                float diff = mouseX - (x + width / 2.0f - 10.0f);
                float multiplier = diff / w;
                setting.invokeValue((int) roundNumber(Math.max(setting.min, Math.min(setting.max, (multiplier * (setting.max - setting.min)) + setting.min))));
            }
        }

        height = 15.0f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (!insideSlider(mouseX, mouseY)) return;
            clickedSlider = true;
        }
    }

    private boolean insideSlider(int mouseX, int mouseY) {
        return mouseX > x + width / 2.0f - 15.0f && mouseX < x + width - 5.0f && mouseY > y + 5.0f && mouseY < y + height - 5.0f;
    }

    private float roundNumber(float value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.FLOOR).floatValue();
    }
}

