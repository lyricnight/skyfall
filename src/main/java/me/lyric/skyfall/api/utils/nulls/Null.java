package me.lyric.skyfall.api.utils.nulls;

import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.utils.interfaces.Globals;

/**
 * @author lyric
 * used to check for nulls
 */
@UtilityClass
public final class Null implements Globals {
    public static boolean is()
    {
        return mc.thePlayer == null || mc.theWorld == null;
    }
}
