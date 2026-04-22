package me.lyric.skyfall.asm.mixin;

import com.mojang.authlib.GameProfile;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.impl.event.mc.MotionUpdateEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DiscouragedShift")
@Mixin(value = EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends EntityPlayer {
    @Unique
    private double skyfall$oldPosX;
    @Unique
    private double skyfall$oldPosY;
    @Unique
    private double skyfall$oldPosZ;
    @Unique
    private float skyfall$oldYaw;
    @Unique
    private float skyfall$oldPitch;
    @Unique
    private boolean skyfall$oldOnGround;

    public MixinEntityPlayerSP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true)
    public void onUpdatePre(CallbackInfo ci) {

        this.skyfall$oldPosX = this.posX;
        this.skyfall$oldPosY = this.posY;
        this.skyfall$oldPosZ = this.posZ;

        this.skyfall$oldYaw = this.rotationYaw;
        this.skyfall$oldPitch = this.rotationPitch;

        this.skyfall$oldOnGround = this.onGround;
        //we handle posting like this since it gives the listener extra time by a few nanoseconds to check if it's cancelled.
        MotionUpdateEvent.Pre motionUpdateEvent = new MotionUpdateEvent.Pre(this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ, this.rotationYaw, this.rotationPitch, this.onGround);
        EventBus.getInstance().post(motionUpdateEvent);
        if (motionUpdateEvent.isCancelled()) ci.cancel();

        this.posX = motionUpdateEvent.getX();
        this.posY = motionUpdateEvent.getY();
        this.posZ = motionUpdateEvent.getZ();

        this.rotationYaw = motionUpdateEvent.getYaw();
        this.rotationPitch = motionUpdateEvent.getPitch();

        this.onGround = motionUpdateEvent.isOnGround();
    }

    @Inject(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    public void onUpdatePost(CallbackInfo ci) {
        this.posX = this.skyfall$oldPosX;
        this.posY = this.skyfall$oldPosY;
        this.posZ = this.skyfall$oldPosZ;

        this.rotationYaw = this.skyfall$oldYaw;
        this.rotationPitch = this.skyfall$oldPitch;

        this.onGround = this.skyfall$oldOnGround;

        MotionUpdateEvent.Post motionUpdateEvent = new MotionUpdateEvent.Post(posX, posY, posZ, motionX, motionY, motionZ, rotationYaw, rotationPitch, onGround);
        EventBus.getInstance().post(motionUpdateEvent);
        if (motionUpdateEvent.isCancelled()) ci.cancel();

        this.posX = motionUpdateEvent.getX();
        this.posY = motionUpdateEvent.getY();
        this.posZ = motionUpdateEvent.getZ();

        this.rotationYaw = motionUpdateEvent.getYaw();
        this.rotationPitch = motionUpdateEvent.getPitch();

        this.onGround = motionUpdateEvent.isOnGround();
    }
}
