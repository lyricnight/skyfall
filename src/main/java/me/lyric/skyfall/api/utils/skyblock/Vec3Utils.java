package me.lyric.skyfall.api.utils.skyblock;

import lombok.experimental.UtilityClass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

/**
 * @author maths??
 */
@UtilityClass
public class Vec3Utils {

    public static Vec3 floorVec(Vec3 vec) {
        return new Vec3(Math.floor(vec.xCoord), Math.floor(vec.yCoord), Math.floor(vec.zCoord));
    }

    public static Vec3 multiply(Vec3 vec, double factor) {
        return new Vec3(vec.xCoord * factor, vec.yCoord * factor, vec.zCoord * factor);
    }

    public static BlockPos toBlockPos(Vec3 vec) {
        return new BlockPos(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static Vec3 getPositionEyes(Vec3 pos, boolean sneaking) {
        double eyeHeight = sneaking ? 1.54 : 1.62;
        return new Vec3(pos.xCoord, pos.yCoord + eyeHeight, pos.zCoord);
    }

    public static Vec3 getLook(float yaw, float pitch) {
        double f2 = -Math.cos(-pitch * 0.017453292);
        return new Vec3(
            Math.sin(-yaw * 0.017453292 - Math.PI) * f2,
            Math.sin(-pitch * 0.017453292),
            Math.cos(-yaw * 0.017453292 - Math.PI) * f2
        );
    }

    public static double distanceTo(Vec3 vec1, Vec3 vec2) {
        double dx = vec2.xCoord - vec1.xCoord;
        double dy = vec2.yCoord - vec1.yCoord;
        double dz = vec2.zCoord - vec1.zCoord;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * represents an etherwarp attempt
     */
    public static class EtherPos {
        public final boolean succeeded;
        public final BlockPos pos;
        public final IBlockState state;

        public EtherPos(boolean succeeded, BlockPos pos, IBlockState state) {
            this.succeeded = succeeded;
            this.pos = pos;
            this.state = state;
        }

        public static final EtherPos NONE = new EtherPos(false, null, null);
    }

    /**
     * position and look direction
     */
    public static class PositionLook {
        public final Vec3 pos;
        public final float yaw;
        public final float pitch;

        public PositionLook(Vec3 pos, float yaw, float pitch) {
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}

