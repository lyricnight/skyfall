package me.lyric.skyfall.impl.hud;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.utils.maths.MathsUtils;

public class Latency extends HUDBase {
    public BooleanSetting extend = settingHUD("Extended", true);
    public BooleanSetting lowercase = settingHUD("Lowercase", false);

    public Latency() {
        super("Latency");
    }

    @Override
    public void onRender2D()
    {
        float avgPing = Managers.SERVER.getAveragePing();
        float lastPing = Managers.SERVER.getLastPing();

        String base = lowercase.getValue() ? "ping: " : "Ping: ";
        String latency;
        if (extend.getValue())
        {
            latency = ChatFormatting.GRAY + "" + MathsUtils.round(avgPing, 1) + "ms [" + ChatFormatting.GRAY + MathsUtils.round(lastPing, 1) + "ms]";
        }
        else {
            latency = ChatFormatting.GRAY + "" + MathsUtils.round(avgPing, 1) + "ms";
        }
        renderWithShader(() -> drawHUDString(base, x, y, getRenderColor()));
        drawHUDString(latency, x + getHUDStringWidth(base), y, getRenderColor());

        width = getHUDStringWidth(base + latency);
        height = getHUDStringHeight();
    }

}
