package me.lyric.skyfall.impl.feature.player;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.utils.maths.StopWatch;
import me.lyric.skyfall.api.utils.nulls.Null;
import net.minecraft.item.ItemStack;

/**
 * @author tortle
 * modified by lyric
 */
public final class AutoGFS extends Feature {
    public BooleanSetting tnt = setting("Superboom TNT", false).invokeTab("Values");
    public BooleanSetting pearl = setting("Ender Pearl", false).invokeTab("Values");
    public BooleanSetting jerry = setting("Inflatable Jerry", false).invokeTab("Values");
    public BooleanSetting decoy = setting("Decoy", false).invokeTab("Values");
    public IntegerSetting interval = setting("Delay", 3000, 500, 5000).invokeTab("Timings");
    private final StopWatch timer = new StopWatch.Single();

    public AutoGFS() {
        super("Auto GFS", Category.Player);
    }

    @Override
    public void onTickPre() {
        if (mc.isSingleplayer() || Null.is()) {
            return;
        }
        if (timer.hasBeen(interval.getValue()))
        {
            timer.reset();
            getFromSack("Superboom TNT", 64, tnt.getValue());
            getFromSack("Ender Pearl", 16, pearl.getValue());
            getFromSack("Inflatable Jerry", 64, jerry.getValue());
            getFromSack("Decoy", 64, decoy.getValue());
        }
    }

    private void getFromSack(String item, int stackSize, boolean t) {
        if (Null.is() || !t || !Managers.LOCATION.isOnHypixel()) return;
        for (ItemStack itemStack : mc.thePlayer.inventory.mainInventory) {
            if (itemStack == null) continue;
            if (itemStack.getDisplayName().contains(item) && itemStack.stackSize != stackSize) {
                mc.thePlayer.sendChatMessage("/gfs " + item + " " + (stackSize - itemStack.stackSize));
                break;
            }
        }
    }
}
