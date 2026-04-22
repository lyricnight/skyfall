package me.lyric.skyfall.impl.event.mc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyric.skyfall.api.event.Event;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Getter
@AllArgsConstructor
public class BlockChangeEvent extends Event {
    private final BlockPos pos;
    private final IBlockState old;
    private final IBlockState update;
    private final World world;
}
