package me.lyric.skyfall.api.utils.render;

import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.impl.feature.internals.Interface;
import net.minecraft.client.gui.GuiMainMenu;

import java.awt.*;

@UtilityClass
public class ColourUtils implements Globals {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Color WHITE = new Color(255, 255, 255, 255);
    private static final Color[] cachedGlobalColor = new Color[2];

    public static Color[] getGlobalColor() {
        if (!(mc.currentScreen instanceof GuiMainMenu))
        {
            cachedGlobalColor[0] = TRANSPARENT;
            cachedGlobalColor[1] = WHITE;
        } else {
            cachedGlobalColor[0] = Managers.FEATURES.get(Interface.class).background.getValue();
            cachedGlobalColor[1] = Managers.FEATURES.get(Interface.class).colourSetting.getValue();
        }
        return cachedGlobalColor;
    }
}
