package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.feature.miscellaneous.Fixes;
import me.lyric.skyfall.impl.manager.Location;
import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityEnderman.class)
public class MixinEndermanEntity {
    @Inject(method = "teleportRandomly", at = @At("HEAD"), cancellable = true)
    public void cancelTeleport(CallbackInfoReturnable<Boolean> cir) {
        if (Managers.LOCATION.getCurrentIsland() == Location.Island.DUNGEON && Managers.FEATURES.get(Fixes.class).isEnabled() && Managers.FEATURES.get(Fixes.class).enderman.getValue())
        {
            cir.cancel();
        }
    }
}
