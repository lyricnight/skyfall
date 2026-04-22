package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.miscellaneous.Fixes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockStairs.class)
public abstract class MixinBlockStairs extends Block {
    public MixinBlockStairs(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "doesSideBlockRendering", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getValue(Lnet/minecraft/block/properties/IProperty;)Ljava/lang/Comparable;", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void checkRendering(IBlockAccess world, BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> cir, IBlockState iblockstate) {
        if (Managers.FEATURES.get(Fixes.class).sand.getValue() && !(iblockstate.getBlock() instanceof BlockStairs)) cir.setReturnValue(true);
    }
}
