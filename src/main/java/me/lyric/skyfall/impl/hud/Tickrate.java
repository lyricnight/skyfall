package me.lyric.skyfall.impl.hud;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.utils.maths.MathsUtils;

public class Tickrate extends HUDBase {

    public BooleanSetting extend = settingHUD("Extended", true);

    public BooleanSetting lowercase = settingHUD("Lowercase", false);

    public Tickrate() {
        super("Tickrate");
    }

    @Override
    public void onRender2D()
    {
        String base = "TPS: ";
        String tpsString;
        if (extend.getValue())
        {
            tpsString = ChatFormatting.GRAY + "" + MathsUtils.round(Managers.SERVER.getTps(), 2) + " [" + ChatFormatting.GRAY + MathsUtils.round(Managers.SERVER.getCurrentTps(), 2) + "]";
        }
        else {
            tpsString = ChatFormatting.GRAY + "" + MathsUtils.round(Managers.SERVER.getTps(), 2);
        }

        if (lowercase.getValue()) {
            base = base.toLowerCase();
        }

        final String finalBase = base;
        renderWithShader(() -> drawHUDString(finalBase, x, y, getRenderColor()));
        drawHUDString(tpsString, x + getHUDStringWidth(base), y, getRenderColor());
        width = getHUDStringWidth(base + tpsString);
        height = getHUDStringHeight();
    }
}
