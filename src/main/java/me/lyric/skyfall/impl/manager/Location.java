package me.lyric.skyfall.impl.manager;

import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.maths.StopWatch;
import me.lyric.skyfall.api.utils.skyblock.TabUtils;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ely
 */
public final class Location implements Globals {
    private final StopWatch timer = new StopWatch.Single();
    private Island currentIsland = Island.UNKNOWN;
    @Getter
    private boolean onHypixel = false;
    private final Pattern pattern = Pattern.compile("^(?:Area|Dungeon):\\s*(.+)$");


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            onHypixel = !event.isLocal && (
                    (mc.thePlayer != null && mc.thePlayer.getClientBrand() != null && mc.thePlayer.getClientBrand().toLowerCase().contains("hypixel")) ||
                            (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel"))
            );
        } catch (Exception e) {
            ExceptionHandler.handle(e, this.getClass());
            Skyfall.LOGGER.error("Set onHypixel to false due to exception");
            onHypixel = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldChange(WorldEvent.Unload event) {
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            if (feature.isEnabled()) {
                feature.onWorldChange();
            }
        }
        currentIsland = Island.UNKNOWN;
    }

    public Island getCurrentIsland() {
        if (currentIsland.isArea(Island.UNKNOWN)) {
            currentIsland = checkIsland();
        }
        return currentIsland;
    }

    public Island checkIsland() {
        if (mc.isSingleplayer())  {
            return Island.SINGLEPLAYER;
        }
        if (!onHypixel)
        {
            return Island.UNKNOWN;
        }
        for (String tabName : TabUtils.getTabList()) {
            Matcher matcher = pattern.matcher(tabName);
            if (matcher.find()) {
                for (Island island : Island.values()) {
                    if (matcher.group(1).equals(island.getDisplayName()))
                    {
                        return island;
                    }
                }
            }
        }
        return Island.UNKNOWN;
    }

    @Getter
    public enum Island {
        SINGLEPLAYER("Singleplayer"),
        PRIVATE_ISLAND("Private Island"),
        GARDEN("The Garden"),
        SPIDERS_DEN("Spider's Den"),
        CRIMSON_ISLE("Crimson Isle"),
        THE_END("The End"),
        GOLD_MINE("Gold Mine"),
        DEEP_CAVERNS("Deep Caverns"),
        DWARVEN_MINES("Dwarven Mines"),
        CRYSTAL_HOLLOWS("Crystal Hollows"),
        FARMING_ISLANDS("The Farming Islands"),
        THE_PARK("The Park"),
        DUNGEON("Catacombs"),
        DUNGEON_HUB("Dungeon Hub"),
        HUB("Hub"),
        DARK_AUCTION("Dark Auction"),
        JERRYS_WORKSHOP("Jerry's Workshop"),
        KUUDRA("Kuudra"),
        MINESHAFT("Mineshaft"),
        UNKNOWN("(Unknown)");

        private final String displayName;

        Island(String displayName) {
            this.displayName = displayName;
        }

        public boolean isArea(Island area) {
            return this == area;
        }
    }

}
