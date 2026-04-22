package me.lyric.skyfall.api.utils.skyblock;

import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.string.StringUtils;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class TabUtils implements Globals {

    public static List<String> getTabList() {
        if (mc.ingameGUI.getTabList() == null) return Collections.emptyList();
        Collection<NetworkPlayerInfo> list = mc.thePlayer.sendQueue.getPlayerInfoMap();

        List<String> names = new ArrayList<>();

        for (NetworkPlayerInfo info : list) {
            names.add(StringUtils.removeFormatting(mc.ingameGUI.getTabList().getPlayerName(info)));
        }
        return names;
    }
}
