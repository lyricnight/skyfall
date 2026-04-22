package me.lyric.skyfall.impl.event.mc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyric.skyfall.api.event.Event;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * @author lyric
 * Event fired when player attempts to interact with blocks (break/place)
 */
@Getter
@AllArgsConstructor
public class BlockInteractionEvent extends Event {
    private final BlockPos pos;
    private final EnumFacing side;

    public static class BreakBlock extends BlockInteractionEvent {
        public BreakBlock(BlockPos pos, EnumFacing side) {
            super(pos, side);
        }
    }

    public static class PlaceBlock extends BlockInteractionEvent {
        public PlaceBlock(BlockPos pos, EnumFacing side) {
            super(pos, side);
        }
    }

    public static class StartBreaking extends BlockInteractionEvent {
        public StartBreaking(BlockPos pos, EnumFacing side) {
            super(pos, side);
        }
    }
}
