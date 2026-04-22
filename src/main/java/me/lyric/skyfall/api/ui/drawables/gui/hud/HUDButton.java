package me.lyric.skyfall.api.ui.drawables.gui.hud;

import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.HUDEditorScreen;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.ui.drawables.gui.settings.impl.*;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;

import java.awt.*;
import java.util.ArrayList;

public class HUDButton implements Drawable {
    private final ArrayList<SettingDrawable> settingDrawables = new ArrayList<>();
    private final HUDBase hudModule;
    public float guiX, guiY, guiWidth, guiHeight;
    public float x, y, targetX, targetY, width, height, anim, enabledAnim;
    private boolean open;

    public HUDButton(HUDBase hudModule) {
        this.hudModule = hudModule;
        for (Setting<?> setting : hudModule.getSettingsHUD()) {
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
        x = MathsUtils.lerp(x, targetX, Interface.getDelta());
        y = MathsUtils.lerp(y, targetY, Interface.getDelta());
        RenderUtils.prepareScissor(guiX, guiY, guiX + guiWidth, guiY + guiHeight);

        RenderUtils.rounded(x, y, x + width, y + height, 5.0f, HUDEditorScreen.shade(5));
        RenderUtils.roundedOutline(x, y, x + width, y + 20.0f, 5.0f, HUDEditorScreen.shade(-3));
        RenderUtils.roundedOutline(x, y, x + width, y + height, 5.0f, HUDEditorScreen.shade(-3));
        RenderUtils.roundedOutline(x, y, x + width, y + height, 5.0f, new Color(Interface.primary().getRed() / 255.0f, Interface.primary().getGreen() / 255.0f, Interface.primary().getBlue() / 255.0f, enabledAnim));


        enabledAnim = MathsUtils.lerp(enabledAnim, hudModule.getEnabled() ? 1.0f : 0.0f, Interface.getDelta());

        Managers.TEXT.guiString(hudModule.getName(), x + 5.0f, y + 10.0f - Managers.TEXT.stringHeight() / 2.0f, Color.WHITE);

        RenderUtils.releaseScissor();

        anim = MathsUtils.lerp(anim, open ? 1.0f : 0.0f, Interface.getDelta());

        float deltaY = y + 25.0f;
        if (anim > 0.05f) {
            for (SettingDrawable settingDrawable : settingDrawables) {
                if (settingDrawable instanceof ModeButton && (!settingDrawable.getSetting().visible() || !open)) {
                    ((ModeButton) settingDrawable).open = false;
                }
                if (settingDrawable instanceof ColorButton && (!settingDrawable.getSetting().visible() || !open)) {
                    ((ColorButton) settingDrawable).open = false;
                }
                settingDrawable.x = x + 5.0f;
                settingDrawable.y = deltaY - settingDrawable.getHeight() * (1.0f - settingDrawable.updateVisible());
                settingDrawable.width = width - 10.0f;
                settingDrawable.guiX = guiX;
                settingDrawable.guiY = guiY;
                settingDrawable.guiWidth = guiWidth;
                settingDrawable.guiHeight = guiHeight;
                RenderUtils.prepareScissor(guiX, Math.max(guiY, Math.min(guiY + guiHeight, Math.max(y + 20.0f, deltaY - 3))), guiX + guiWidth, Math.max(guiY, Math.min(guiY + guiHeight, Math.max(deltaY - 3, y + height))));
                settingDrawable.drawScreen(mouseX, mouseY, partialTicks);
                RenderUtils.releaseScissor();
                deltaY += settingDrawable.getHeight() * settingDrawable.visibleAnim;
            }
        }
        height = 20.0f + ((deltaY - y - 15.0f) * anim);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1 && inside(mouseX, mouseY)) {
            open = !open;
        }
        for (SettingDrawable settingDrawable : settingDrawables) {
            if (settingDrawable.visibleAnim > 0.9f) {
                if (settingDrawable instanceof BooleanButton && settingDrawable.getSetting().getName().equals("Enabled")) {
                    if (mouseButton == 0 && ((BooleanButton) settingDrawable).insideEnabled(mouseX, mouseY)) {
                        hudModule.toggle();
                        continue;
                    }
                }
                settingDrawable.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        
    }

    private boolean inside(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
