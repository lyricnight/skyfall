package me.lyric.skyfall.impl.feature.miscellaneous;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.utils.render.MathAnimation;
import net.minecraft.client.gui.ChatLine;

import java.util.HashMap;
import java.util.Map;

public final class Chat extends Feature {

    public BooleanSetting animated = setting("Animated", true).invokeTab("Animations");

    public IntegerSetting time = setting("Time", 200, 1, 500).invokeTab("Animations").invokeVisibility(v -> animated.getValue());

    public BooleanSetting clean = setting("Clean", true).invokeTab("Misc");

    public BooleanSetting infinite = setting("Infinite", false).invokeTab("Misc");

    public final Map<ChatLine, MathAnimation> animationMap = new HashMap<>();

    public Chat()
    {
        super("Chat", Category.Miscellaneous);
    }
}
