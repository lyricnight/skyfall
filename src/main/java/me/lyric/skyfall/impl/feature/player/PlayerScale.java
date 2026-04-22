package me.lyric.skyfall.impl.feature.player;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.FloatSetting;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;

/**
 * @author lyric
 * LOL
 */
public final class PlayerScale extends Feature {

    public BooleanSetting baby = setting("Baby", false).invokeTab("Modifiers");

    public BooleanSetting global = setting("Global", false);
    public FloatSetting scaleX = setting("ScaleX", 1.0f, -3f, 3f).invokeTab("Scale");
    public FloatSetting scaleY = setting("ScaleY", 1.0f, -3f, 3f).invokeTab("Scale");
    public FloatSetting scaleZ = setting("ScaleZ", 1.0f, -3f, 3f).invokeTab("Scale");

    public PlayerScale() {
        super("PlayerScale", Category.Player);
    }

    public void onScalePlayerHook(AbstractClientPlayer player)
    {
        if (this.isEnabled())
        {
            if (!global.getValue() && player != mc.thePlayer) return;
            if (scaleY.getValue() < 0) GlStateManager.translate(0f, scaleY.getValue() * 2, 0f);
            GlStateManager.scale(scaleX.getValue(), scaleY.getValue(), scaleZ.getValue());
        }
    }
}
