package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.FloatSetting;

public final class Aspect extends Feature {
    public FloatSetting aspect = setting("Aspect", (float) mc.displayWidth / mc.displayHeight + 0f, 0.1f, 3.0f).invokeTab("Aspect");

    public Aspect() {
        super("Aspect", Category.Render);
    }

    @Override
    public String displayAppend()
    {
        return aspect.getValue().toString();
    }
}
