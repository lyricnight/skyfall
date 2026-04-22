package me.lyric.skyfall.api.ui.drawables.gui.settings;


import lombok.Getter;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.utils.maths.MathsUtils;

public class SettingDrawable implements Drawable {
    public float guiX, guiY, guiWidth, guiHeight;
    @Getter
    public float x, y, width, height;
    @Getter
    public final Setting<?> setting;
    public float visibleAnim;

    public SettingDrawable(Setting<?> setting) {
        this.setting = setting;
        this.visibleAnim = isVisible() ? 1.0f : 0.0f;
    }

    public float updateVisible() {
        return visibleAnim = MathsUtils.lerp(visibleAnim, isVisible() ? 1.0f : 0.0f, Interface.getDelta());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    public boolean isVisible() {
        return setting.visible();
    }
}
