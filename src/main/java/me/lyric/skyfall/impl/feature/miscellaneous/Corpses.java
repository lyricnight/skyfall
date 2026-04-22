package me.lyric.skyfall.impl.feature.miscellaneous;

import com.mojang.realmsclient.gui.ChatFormatting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.utils.maths.StopWatch;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.api.utils.render.FeatureRenderUtils;
import me.lyric.skyfall.impl.manager.Location;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * scans and marks corpses in mineshafts.
 * @author lyric
 */
public final class Corpses extends Feature {
    public IntegerSetting scanDelay = setting("Scan Delay", 2000, 1000, 5000).invokeTab("Timing");

    public BooleanSetting notify = setting("Notify", true).invokeTab("Misc");

    private final Set<Corpse> corpses = new HashSet<>();

    private final StopWatch.Single timer = new StopWatch.Single();

    public Corpses() {
        super("Corpses", Category.Miscellaneous);
    }

    @Override
    public void onWorldChange() {
        corpses.clear();
    }

    @Override
    public void onDisable() {
        corpses.clear();
    }

    @Override
    public void onTickPre()
    {
        if (Null.is() || Managers.LOCATION.getCurrentIsland() != Location.Island.MINESHAFT) return;
        if (timer.hasBeen(scanDelay.getValue()))
        {
            timer.reset();
            scan();
        }
    }

    @Override
    public void onRender3D()
    {
        if (corpses.isEmpty()) return;
        for (Corpse corpse : corpses) {
            Vec3 pos = corpse.getPosition();
            Color color = getColorForCorpseType(corpse.getType());
            FeatureRenderUtils.renderBox(
                pos.xCoord - 0.5,
                pos.yCoord,
                pos.zCoord - 0.5,
                1.0,
                2.0,
                1.0,
                color,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 50),
                2.0f,
                true,
                true,
                true
            );
        }
    }

    private Color getColorForCorpseType(CorpseType type) {
        switch (type) {
            case LAPIS:
                return new Color(0, 100, 255);
            case UMBER:
                return new Color(255, 215, 0);
            case TUNGSTEN:
                return new Color(128, 128, 128);
            case VANGUARD:
                return new Color(160, 32, 240);
            default:
                return Color.WHITE;
        }
    }

    private void scan()
    {
        if (mc.theWorld.loadedEntityList == null) return;
        mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityArmorStand)
                .filter(entity -> corpses.stream().noneMatch(corpse -> corpse.getPosition().distanceTo(entity.getPositionVector()) < 3.0))
                .filter(entity -> ((EntityArmorStand) entity).getShowArms() && ((EntityArmorStand) entity).hasNoBasePlate() && !entity.isInvisible())
                .forEach(
                        entity -> {
                            if (((EntityArmorStand) entity).getEquipmentInSlot(4) == null) return; //needed here because they spawn as empty armor stands first
                            String name = ((EntityArmorStand) entity).getEquipmentInSlot(4).getDisplayName();
                            if (name.contains("Lapis"))
                            {
                                addCorpseType(CorpseType.LAPIS, entity.getPositionVector());
                            }
                            else if (name.contains("Yog"))
                            {
                                addCorpseType(CorpseType.UMBER, entity.getPositionVector());
                            }
                            else if (name.contains("Mineral"))
                            {
                                addCorpseType(CorpseType.TUNGSTEN, entity.getPositionVector());
                            }
                            else if (name.contains("Vanguard"))
                            {
                                addCorpseType(CorpseType.VANGUARD, entity.getPositionVector());
                            }
                        }
                );
    }

    private void addCorpseType(CorpseType corpseType, Vec3 position) {
        if (notify.getValue())
        {
            Managers.MESSAGES.send("Located " + corpseType.getDisplayName() + " at " + "(" + position.xCoord + ", " + position.yCoord + ", " + position.zCoord + ") !");
        }
        corpses.add(new Corpse(corpseType, position));
    }

    @AllArgsConstructor
    @Getter
    private static class Corpse {
        private final CorpseType type;
        private final Vec3 position;
    }

    private enum CorpseType {
        LAPIS(ChatFormatting.BLUE + "Lapis Corpse" + ChatFormatting.RESET),
        UMBER(ChatFormatting.GOLD + "Umber Corpse" + ChatFormatting.RESET),
        TUNGSTEN(ChatFormatting.GRAY + "Tungsten Corpse" + ChatFormatting.RESET),
        VANGUARD(ChatFormatting.LIGHT_PURPLE + "Vanguard Corpse" + ChatFormatting.RESET);

        @Getter
        private final String displayName;

        CorpseType(String displayName) {
            this.displayName = displayName;
        }
    }
}
