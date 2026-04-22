package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.player.PlayerScale;
import me.lyric.skyfall.impl.feature.render.NoRender;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    public void isPotionActive(Potion potion, CallbackInfoReturnable<Boolean> cir) {
        if (Managers.FEATURES.get(NoRender.class).noNausea.getValue() && potion == Potion.confusion) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isChild", at = @At("HEAD"), cancellable = true)
    public void setChildHook(CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(skyfall$modify());
    }

    @Unique
    private boolean skyfall$modify() {
        //noinspection ConstantValue
        if ((Entity)this instanceof EntityOtherPlayerMP || (Entity)this instanceof EntityPlayerSP)
        {
            return Managers.FEATURES.get(PlayerScale.class).isEnabled() && Managers.FEATURES.get(PlayerScale.class).baby.getValue();
        }
        return false;
    }
}
