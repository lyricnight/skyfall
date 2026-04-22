package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.ColourSetting;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;

public final class Sky extends Feature {

    public ModeSetting mode = setting("Mode", "Time", Arrays.asList("Time", "Custom")).invokeTab("Mode");

    public IntegerSetting time = setting("Time", 1000, 0, 24000).invokeTab("Misc").invokeVisibility(v -> mode.getValue().equals("Time"));

    public ColourSetting colour = setting("Colour", Color.BLACK).invokeTab("Misc").invokeVisibility(v -> mode.getValue().equals("Custom"));

    public BooleanSetting fog = setting("Fog", false).invokeTab("Fog");

    public ColourSetting fogColour = setting("Fog Colour", Color.BLACK).invokeTab("Fog").invokeVisibility(v -> fog.getValue());


    public Sky() {
        super("Sky", Category.Render);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFogRender(EntityViewRenderEvent.FogColors event)
    {
        if (fog.getValue())
        {
            event.red = fogColour.getValue().getRed() / 255f;
            event.green = fogColour.getValue().getGreen() / 255f;
            event.blue = fogColour.getValue().getBlue() / 255f;
        }
    }

    @Override
    public String displayAppend()
    {
        return " " + mode.getValue() + ", " + fog.getValue().toString();
    }
}
