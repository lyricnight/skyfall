package me.lyric.skyfall.api.utils.maths;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class Time {
    public static final long NANOS_PER_MS = TimeUnit.MILLISECONDS.toNanos(1L);

    public static long getMillis() {
        return System.nanoTime() / NANOS_PER_MS;
    }

    public static boolean isTimePointOlderThan(long timePoint, long ms) {
        return getPassedTimeSince(timePoint) >= ms;
    }

    public static long getPassedTimeSince(long timePoint) {
        return getMillis() - timePoint;
    }
}