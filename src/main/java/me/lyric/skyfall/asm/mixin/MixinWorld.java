package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.render.Sky;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    public void skyColorHook(Entity entityIn, float partialTicks, CallbackInfoReturnable<Vec3> cir)
    {
        if (Managers.FEATURES.get(Sky.class).isEnabled() && Managers.FEATURES.get(Sky.class).mode.getValue().equals("Custom"))
        {
            Sky feature = Managers.FEATURES.get(Sky.class);
            cir.cancel();
            cir.setReturnValue(new Vec3(feature.colour.getValue().getRed() / 255f, feature.colour.getValue().getGreen() / 255f, feature.colour.getValue().getBlue() / 255f));
        }
    }

    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    public void getWorldTimeHook(CallbackInfoReturnable<Long> cir)
    {
        if (Managers.FEATURES.get(Sky.class).isEnabled() && Managers.FEATURES.get(Sky.class).mode.getValue().equals("Time"))
        {
            cir.cancel();
            cir.setReturnValue((long) Managers.FEATURES.get(Sky.class).time.getValue());
        }
    }

}
