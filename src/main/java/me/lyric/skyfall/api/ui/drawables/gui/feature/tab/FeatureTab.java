package me.lyric.skyfall.api.ui.drawables.gui.feature.tab;

import lombok.Getter;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.ui.drawables.gui.settings.impl.*;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;

import java.awt.*;
import java.util.ArrayList;

public class FeatureTab implements Drawable {
    public final ArrayList<Setting<?>> settings = new ArrayList<>();
    public final ArrayList<SettingDrawable> settingDrawables = new ArrayList<>();
    @Getter
    public float x = -0.26969f, y = -0.26969f, width, height;
    public float guiX, guiY, guiWidth, guiHeight;
    public float targetX, targetY, anim;
    public final Feature feature;
    @Getter
    public String name;

    public FeatureTab(Feature feature, String name) {
        this.feature = feature;
        this.name = name;
    }

    public void init() {
        for (Setting<?> setting : settings) {
            if (setting instanceof BooleanSetting) {
                settingDrawables.add(new BooleanButton((BooleanSetting) setting));
                continue;
            }
            if (setting instanceof BindSetting) {
                settingDrawables.add(new BindButton((BindSetting) setting));
                continue;
            }
            if (setting instanceof FloatSetting) {
                settingDrawables.add(new FloatButton((FloatSetting) setting));
                continue;
            }
            if (setting instanceof IntegerSetting) {
                settingDrawables.add(new IntButton((IntegerSetting) setting));
                continue;
            }
            if (setting instanceof ModeSetting) {
                settingDrawables.add(new ModeButton((ModeSetting) setting));
                continue;
            }
            if (setting instanceof StringSetting) {
                settingDrawables.add(new StringButton((StringSetting) setting));
                continue;
            }
            if (setting instanceof ActionSetting) {
                settingDrawables.add(new ActionButton((ActionSetting) setting));
                continue;
            }
            settingDrawables.add(new ColorButton((ColourSetting) setting));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        anim = MathsUtils.lerp(anim, (DefaultScreen.getActiveFeature() != null && DefaultScreen.getActiveFeature() == feature) ? 0.0f : 1.0f, Interface.getDelta());
        if (ignore()) {
            return;
        }

        x = MathsUtils.lerp(x, targetX, Interface.getDelta());
        y = MathsUtils.lerp(y, targetY, Interface.getDelta());

        RenderUtils.prepareScissor(guiX, guiY, guiX + guiWidth, guiY + guiHeight);

        Managers.TEXT.guiString(name, x + 5.0f, y - 9f, Color.GRAY);
        RenderUtils.rounded(x, y, x + width, y + height, 7.0f, color());
        RenderUtils.roundedOutline(x, y, x + width, y + height, 7.0f, shade(-3));

        RenderUtils.releaseScissor();

        float deltaY = y + 5.0f;
        for (SettingDrawable settingDrawable : settingDrawables) {
            settingDrawable.x = x + 5.0f;
            settingDrawable.y = deltaY - settingDrawable.getHeight() * (1.0f - settingDrawable.updateVisible());
            settingDrawable.width = width - 10.0f;
            settingDrawable.guiX = guiX;
            settingDrawable.guiY = guiY;
            settingDrawable.guiWidth = guiWidth;
            settingDrawable.guiHeight = guiHeight;
            RenderUtils.prepareScissor(guiX, Math.max(guiY, Math.min(guiY + guiHeight, deltaY - 3)), guiX + guiWidth, Math.max(guiY, Math.min(guiY + guiHeight, Math.max(deltaY - 3, y + height))));
            settingDrawable.drawScreen(mouseX, mouseY, partialTicks);
            RenderUtils.releaseScissor();
            deltaY += settingDrawable.getHeight() * settingDrawable.updateVisible();
        }
        height = deltaY - y + 5.0f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (ignore() || mouseX < guiX || mouseX > guiX + guiWidth || mouseY < guiY || mouseY > guiY + guiHeight) {
            return;
        }
        settingDrawables.stream().filter(settingDrawable -> settingDrawable.visibleAnim > 0.9f).forEach(settingDrawable -> settingDrawable.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (ignore()) {
            return;
        }
        settingDrawables.stream().filter(SettingDrawable::isVisible).forEach(settingDrawable -> settingDrawable.keyTyped(typedChar, keyCode));
    }

    public static Color color() {
        //this is a scaling experiment.
        Color get = Interface.background();
        if (get.getRed() + 15 > 255 || get.getGreen() + 15 > 255 || get.getBlue() + 22 > 255) {
            return get;
        }
        return new Color(Interface.background().getRed() + 15, Interface.background().getGreen() + 15, Interface.background().getBlue() + 22);
    }

    public static Color shade(int i) {
        if (color().getRed() + i > 255 || color().getGreen() + i > 255 || color().getBlue() + i > 255 || color().getRed() + i < 0 || color().getGreen() + i < 0 || color().getBlue() + i < 0)
        {
            return color();
        }
        return new Color(color().getRed() + i, color().getGreen() + i, color().getBlue() + i);
    }

    public boolean ignore() {
        return anim > 0.9f;
    }
}
