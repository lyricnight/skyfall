package me.lyric.skyfall.api.utils.maths;

import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author java devs and common sense
 * generic mathemtical utilities.
 */
@UtilityClass
public final class MathsUtils implements Globals {
    public static double lerp(double current, double target, double lerp) {
        current -= (current - target) * MathHelper.clamp_double(lerp, 0, 1);
        return current;
    }

    public static float lerp(float current, float target, float lerp) {
        return current - ((current - target) * clamp(lerp, 0, 1));
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float gaussian(float x, float s) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (s * s));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (s * s))));
    }

    public static Vector3f mix(Vector3f first, Vector3f second, float factor)
    {
        return new Vector3f(first.x * (1.0f - factor) + second.x * factor, first.y * (1.0f - factor) + second.y * factor, first.z * (1.0f - factor) + first.z * factor);
    }

    public static float round(float value, int places) {
        return places < 0 ? value : (new BigDecimal(value)).setScale(places, RoundingMode.HALF_UP).floatValue();
    }
}
