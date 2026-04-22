package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.event.mc.BlockInteractionEvent;
import me.lyric.skyfall.impl.feature.player.Drill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * @author lyric
 * again, max value priority thanks to oringo's one not working since they can't be bothered to update it.
 */
@Mixin(value = PlayerControllerMP.class, priority = Integer.MAX_VALUE)
public class MixinPlayerControllerMP {
    @Shadow private ItemStack currentItemHittingBlock;

    @Shadow private BlockPos currentBlock;

    /**
     * @author lyric
     * @reason fix drill reset bug.
     */
    @Overwrite
    private boolean isHittingPosition(BlockPos pos) {
        return Managers.FEATURES.get(Drill.class).isHittingPosition(pos, currentItemHittingBlock, currentBlock);
    }

    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    private void onClickBlock(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        BlockInteractionEvent.StartBreaking event = new BlockInteractionEvent.StartBreaking(pos, side);
        EventBus.getInstance().post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDestroyBlock(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        BlockInteractionEvent.BreakBlock event = new BlockInteractionEvent.BreakBlock(pos, side);
        EventBus.getInstance().post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onPlayerRightClick", at = @At("HEAD"), cancellable = true)
    private void onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.equals(player.getName(), Minecraft.getMinecraft().thePlayer.getName())) {
            BlockInteractionEvent.PlaceBlock event = new BlockInteractionEvent.PlaceBlock(hitPos, side);
            EventBus.getInstance().post(event);
            if (event.isCancelled()) {
                cir.setReturnValue(false);
            }
        }
    }
}
