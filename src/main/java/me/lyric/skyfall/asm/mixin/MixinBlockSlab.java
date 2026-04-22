package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.miscellaneous.Fixes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockSlab.class)
public abstract class MixinBlockSlab extends Block {
    public MixinBlockSlab(net.minecraft.block.material.Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "doesSideBlockRendering", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getValue(Lnet/minecraft/block/properties/IProperty;)Ljava/lang/Comparable;", ordinal = 0), cancellable = true)
    private void checkRendering(IBlockAccess world, BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        if (Managers.FEATURES.get(Fixes.class).sand.getValue() && !(world.getBlockState(pos).getBlock() instanceof BlockSlab))
            cir.setReturnValue(true);
    }
}