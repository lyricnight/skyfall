package me.lyric.skyfall.impl.hud;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.setting.types.BooleanSetting;

public class Watermark extends HUDBase {

    public BooleanSetting lower = settingHUD("Lowercase", false);

    public Watermark()
    {
        super("Watermark");
        x = 1f;
        y = 1f;
    }

    @Override
    public void onRender2D()
    {
        String text = Skyfall.NAME + " " + Skyfall.VERSION;
        if (lower.getValue()) {
            text = text.toLowerCase();
        }

        final String finalText = text;
        renderWithShader(() -> drawHUDString(finalText, x, y, getRenderColor()));

        width = getHUDStringWidth(text);
        height = getHUDStringHeight();
    }
}
