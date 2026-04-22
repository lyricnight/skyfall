package me.lyric.skyfall.api.utils.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MathHelper;

/**
 * @author lyric
 * basically my own easing function for stuff - might use this in random places
 */
public final class MathAnimation {
    @Getter
    private final long length;
    @Getter
    private final double start;
    @Getter
    private final double end;
    @Setter
    @Getter
    private double current;
    private double progress;
    @Getter
    private boolean playing;
    @Getter
    private boolean backwards;
    private boolean reverseOnEnd;
    private final long startTime;
    private long lastTime;
    private double per;
    private final long dif;
    private boolean flag;

    @Setter
    @Getter
    private AnimationMode mode;

    public MathAnimation(long length, double start, double end, boolean backwards, AnimationMode mode) {
        this.length = length;
        this.start = start;
        current = start;
        this.end = end;
        this.mode = mode;
        this.backwards = backwards;
        startTime = System.currentTimeMillis();
        playing = true;
        dif = (System.currentTimeMillis() - startTime);
        switch (mode) {
            case LINEAR:
                per = (end - start) / length;
                break;
            case EXPONENTIAL:
                double dif = end - start;
                flag = dif < 0;
                if (flag) dif *= -1;
                for (int i = 0; i < length; i++) {
                    dif = Math.sqrt(dif);
                }
                per = dif;
                break;
        }
        lastTime = System.currentTimeMillis();
    }

    public MathAnimation(long length, double start, double end, boolean backwards, boolean reverseOnEnd, AnimationMode mode) {
        this(length, start, end, backwards, mode);
        this.reverseOnEnd = reverseOnEnd;
    }

    public void add() {
        if (playing) {
            if (mode == AnimationMode.LINEAR) {
                current = start + progress;
                progress += per * (System.currentTimeMillis() - lastTime);
            } else if (mode == AnimationMode.EXPONENTIAL) {
                /*current = start + per;
                if (lastDif != dif) {
                    per *= 1.0d + per;
                    if (flag && per > 0) per *= -1;
                }*/
            }
            current = MathHelper.clamp_double(current, start, end);
            if (current >= end || (backwards && current <= start)) {
                if (reverseOnEnd) {
                    reverse();
                    reverseOnEnd = false;
                } else {
                    playing = false;
                }
            }
        }
        lastTime = System.currentTimeMillis();
    }

    public void play() {
        playing = true;
    }

    public void stop() {
        playing = false;
    }

    public void reverse() {
        backwards = !backwards;
        per *= -1;
    }

    public enum AnimationMode {
        LINEAR,
        EXPONENTIAL
    }
}
