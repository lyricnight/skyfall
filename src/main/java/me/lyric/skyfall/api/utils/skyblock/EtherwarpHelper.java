package me.lyric.skyfall.api.utils.skyblock;

import lombok.experimental.UtilityClass;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;

import java.util.BitSet;

/**
 * @author lyric
 */
@UtilityClass
public class EtherwarpHelper implements Globals {
    private static final BitSet validEtherwarpFeetIds = new BitSet(256);
    private static final BitSet invalidBlocks = new BitSet(256);

    static {
        Block[] passableBlocks = {
            Blocks.air, Blocks.activator_rail, Blocks.brown_mushroom, Blocks.carpet,
            Blocks.carrots, Blocks.cocoa, Blocks.deadbush, Blocks.double_plant, Blocks.fire,
            Blocks.flower_pot, Blocks.flowing_lava, Blocks.ladder, Blocks.lava, Blocks.lever,
            Blocks.melon_stem, Blocks.nether_wart, Blocks.piston_extension, Blocks.portal,
            Blocks.potatoes, Blocks.powered_comparator, Blocks.powered_repeater, Blocks.pumpkin_stem,
            Blocks.rail, Blocks.red_flower, Blocks.red_mushroom, Blocks.redstone_torch,
            Blocks.redstone_wire, Blocks.reeds, Blocks.sapling, Blocks.skull, Blocks.snow_layer,
            Blocks.stone_button, Blocks.tallgrass, Blocks.torch, Blocks.tripwire, Blocks.tripwire_hook,
            Blocks.unpowered_comparator, Blocks.unpowered_repeater, Blocks.vine, Blocks.water, Blocks.web,
            Blocks.wheat, Blocks.wooden_button, Blocks.yellow_flower, Blocks.end_portal
        };

        for (Block block : passableBlocks) {
            validEtherwarpFeetIds.set(Block.getIdFromBlock(block));
        }

        Block[] interactableBlocks = {
            Blocks.hopper, Blocks.chest, Blocks.ender_chest, Blocks.furnace, Blocks.crafting_table,
            Blocks.cauldron, Blocks.enchanting_table, Blocks.dispenser, Blocks.dropper,
            Blocks.brewing_stand, Blocks.trapdoor
        };

        for (Block block : interactableBlocks) {
            invalidBlocks.set(Block.getIdFromBlock(block));
        }
    }

    /**
     * etherwarp destination position
     * @param positionLook The player's position and look direction
     * @param distance The maximum distance to check
     * @param etherWarp Whether to use etherwarp validation rules
     * @return - what do u think
     */
    public static Vec3Utils.EtherPos getEtherPos(Vec3Utils.PositionLook positionLook, double distance, boolean etherWarp) {
        Vec3 eyePos = Vec3Utils.getPositionEyes(positionLook.pos, mc.thePlayer.isSneaking());
        Vec3 lookVec = Vec3Utils.getLook(positionLook.yaw, positionLook.pitch);
        Vec3 endPos = eyePos.addVector(
            lookVec.xCoord * distance,
            lookVec.yCoord * distance,
            lookVec.zCoord * distance
        );

        return traverseVoxels(eyePos, endPos, etherWarp);
    }

    /**
     * @author bloom
     */
    private static Vec3Utils.EtherPos traverseVoxels(Vec3 start, Vec3 end, boolean etherWarp) {
        double x0 = start.xCoord;
        double y0 = start.yCoord;
        double z0 = start.zCoord;

        double x1 = end.xCoord;
        double y1 = end.yCoord;
        double z1 = end.zCoord;

        Vec3 flooredStart = Vec3Utils.floorVec(start);
        Vec3 flooredEnd = Vec3Utils.floorVec(end);

        int x = (int) flooredStart.xCoord;
        int y = (int) flooredStart.yCoord;
        int z = (int) flooredStart.zCoord;

        int endX = (int) flooredEnd.xCoord;
        int endY = (int) flooredEnd.yCoord;
        int endZ = (int) flooredEnd.zCoord;

        double dirX = x1 - x0;
        double dirY = y1 - y0;
        double dirZ = z1 - z0;

        int stepX = (int) Math.signum(dirX);
        int stepY = (int) Math.signum(dirY);
        int stepZ = (int) Math.signum(dirZ);

        double invDirX = dirX != 0.0 ? 1.0 / dirX : Double.MAX_VALUE;
        double invDirY = dirY != 0.0 ? 1.0 / dirY : Double.MAX_VALUE;
        double invDirZ = dirZ != 0.0 ? 1.0 / dirZ : Double.MAX_VALUE;

        double tDeltaX = Math.abs(invDirX * stepX);
        double tDeltaY = Math.abs(invDirY * stepY);
        double tDeltaZ = Math.abs(invDirZ * stepZ);

        double tMaxX = Math.abs((x + Math.max(stepX, 0) - x0) * invDirX);
        double tMaxY = Math.abs((y + Math.max(stepY, 0) - y0) * invDirY);
        double tMaxZ = Math.abs((z + Math.max(stepZ, 0) - z0) * invDirZ);

        for (int i = 0; i < 1000; i++) {
            if (mc.theWorld == null) return Vec3Utils.EtherPos.NONE;

            Chunk chunk = mc.theWorld.getChunkFromBlockCoords(new BlockPos(x, y, z));
            if (chunk == null) return Vec3Utils.EtherPos.NONE;

            BlockPos currentPos = new BlockPos(x, y, z);
            IBlockState currentState = chunk.getBlockState(currentPos);
            Block currentBlock = currentState.getBlock();
            int currentBlockId = Block.getIdFromBlock(currentBlock);
            if ((!validEtherwarpFeetIds.get(currentBlockId) && etherWarp) || (currentBlockId != 0 && !etherWarp)) {
                if (!etherWarp && validEtherwarpFeetIds.get(currentBlockId)) {
                    return new Vec3Utils.EtherPos(false, currentPos, currentState);
                }

                BlockPos footPos = new BlockPos(x, y + 1, z);
                IBlockState footState = chunk.getBlockState(footPos);
                int footBlockId = Block.getIdFromBlock(footState.getBlock());
                if (!validEtherwarpFeetIds.get(footBlockId)) {
                    return new Vec3Utils.EtherPos(false, currentPos, currentState);
                }

                BlockPos headPos = new BlockPos(x, y + 2, z);
                IBlockState headState = chunk.getBlockState(headPos);
                int headBlockId = Block.getIdFromBlock(headState.getBlock());
                if (!validEtherwarpFeetIds.get(headBlockId)) {
                    return new Vec3Utils.EtherPos(false, currentPos, currentState);
                }

                return new Vec3Utils.EtherPos(true, currentPos, currentState);
            }

            if (x == endX && y == endY && z == endZ) {
                return Vec3Utils.EtherPos.NONE;
            }

            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                tMaxX += tDeltaX;
                x += stepX;
            } else if (tMaxY <= tMaxZ) {
                tMaxY += tDeltaY;
                y += stepY;
            } else {
                tMaxZ += tDeltaZ;
                z += stepZ;
            }
        }

        return Vec3Utils.EtherPos.NONE;
    }

    public static boolean isInteractableBlock(int blockId) {
        return invalidBlocks.get(blockId);
    }
}
