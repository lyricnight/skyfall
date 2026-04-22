package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;


import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;

import java.awt.*;

public class ModeButton extends SettingDrawable {
    private final ModeSetting setting;
    private float anim, boxWidth;
    public boolean open;

    public ModeButton(ModeSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;
        Managers.TEXT.guiString(setting.getName(), x, y + 7.5f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));
        float longest = 0.0f;
        for (String string : setting.values) {
            float stringWidth = Managers.TEXT.guiStringWidthSmall(string);
            if (stringWidth > longest) {
                longest = stringWidth;
            }
        }

        boxWidth = longest + 20.0f;
        anim = MathsUtils.lerp(anim, open ? 1.0f : 0.0f, Interface.getDelta());
        height = 13.0f + (((10.0f * setting.values.size() - 1) - 2.5f) * anim);

        RenderUtils.rounded(x + width - boxWidth, y + 2.0f, x + width, y + height - 2.0f, 5.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width - boxWidth, y + 2.0f, x + width, y + height - 2.0f, 5.0f, FeatureTab.shade(-2));

        Managers.TEXT.guiStringSmall(setting.getValue(), (x + width - boxWidth / 2.0f - Managers.TEXT.guiStringWidthSmall(setting.getValue()) / 2.0f) , (y + 7f - Managers.TEXT.guiStringHeightSmall() / 2.0f), Color.WHITE);
        RenderUtils.drawExpand(x + width - 12.5f, y + 2.5f, open);

        RenderUtils.prepareScissor(Math.max(guiX, Math.min(x, guiX + guiWidth)), Math.max(guiY, Math.min(guiY + guiHeight, y)), Math.max(guiX, Math.min(x + width, guiX + guiWidth)), Math.max(guiY, Math.min(guiY + guiHeight, y + height - 2.5f)));

        float deltaY = y + 12.5f;
        for (String value : setting.values) {
            if (setting.getValue().equals(value)) {
                continue;
            }
            Color color = new Color(1.0f, 1.0f, 1.0f, 0.4f);
            if ((mouseY > deltaY && mouseY < deltaY + 11.0f) && insideBox(mouseX, mouseY)) {
                color = Interface.primary();
            }
            Managers.TEXT.guiStringSmall(value, (x + width - boxWidth / 2.0f - Managers.TEXT.guiStringWidthSmall(value) / 2.0f), (deltaY + 7.0f - Managers.TEXT.guiStringHeightSmall() / 2.0f), color);
            deltaY += 11.0f;
        }

        RenderUtils.releaseScissor();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (insideBox(mouseX, mouseY)) {
                if (open) {
                    float deltaY = y + 12.5f;
                    for (String value : setting.values) {
                        if (setting.getValue().equals(value)) {
                            continue;
                        }
                        if (mouseY > deltaY && mouseY < deltaY + 11.0f) {
                            setting.invokeValue(value);
                            break;
                        }
                        deltaY += 11.0f;
                    }
                }
                open = !open;
            }
        }
    }

    private boolean insideBox(int mouseX, int mouseY) {
        return mouseX > x + width - boxWidth && mouseX < x + width && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
    }
}
