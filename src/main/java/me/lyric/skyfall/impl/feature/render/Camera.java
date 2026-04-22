package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.utils.player.Position;
import me.lyric.skyfall.impl.event.mc.BlockInteractionEvent;
import me.lyric.skyfall.impl.event.mc.ClickEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author lyric
 * ported to java from kotlin
 * cheers to smenta karta
 */
public final class Camera extends Feature {
    //constants
    private static final double ACCELERATION = 20.0;
    private static final double MAX_SPEED = 35.0;
    private static final double SLOWDOWN = 0.05;
    //modifiers
    private static Vec3 lookVec = new Vec3(0.0, 0.0, 0.0);
    private static double forwardVelocity = 0.0;
    private static double leftVelocity = 0.0;
    private static double upVelocity = 0.0;
    private static long lastTime = 0;
    private static int oldCameraType = 0;
    private static MovementInput oldInput = new MovementInput();
    private static boolean oldNoClip = false;

    private static final Position freeCamPosition = new Position();
    private static final Position playerPosition = new Position();
    private static boolean shouldOverride = false;
    private static boolean entitiesRendering = false;

    public BooleanSetting spectator = setting("Spectator Movement", false).invokeTab("Movement");
    public BooleanSetting verbose = setting("Verbose", false).invokeTab("Debug");

    public Camera() {
        super("Camera", Category.Render);
    }

    @Override
    public void onEnable()
    {
        oldCameraType = mc.gameSettings.thirdPersonView;
        oldInput = mc.thePlayer.movementInput;
        mc.thePlayer.movementInput = new MovementInput();
        mc.gameSettings.thirdPersonView = 0;

        if (mc.getRenderViewEntity() != null) {
            Entity entity = mc.getRenderViewEntity();
            net.minecraft.util.Vec3 pos = entity.getPositionEyes(1f);
            lookVec = getVecFromRotation(entity.rotationPitch, entity.rotationYaw);
            freeCamPosition.setX(pos.xCoord + lookVec.xCoord * -1.5);
            freeCamPosition.setY(pos.yCoord + lookVec.yCoord * -1.5);
            freeCamPosition.setZ(pos.zCoord + lookVec.zCoord * -1.5);
            freeCamPosition.setPitch(entity.rotationPitch);
            freeCamPosition.setYaw(entity.rotationYaw);
        }
        lastTime = System.nanoTime();
    }

    @Override
    public void onDisable()
    {
        mc.gameSettings.thirdPersonView = oldCameraType;
        mc.thePlayer.movementInput = oldInput;
        shouldOverride = false;

        forwardVelocity = 0.0;
        leftVelocity = 0.0;
        upVelocity = 0.0;
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onWorldChange()
    {
        setEnabled(false);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onTickPre()
    {
        while (mc.gameSettings.keyBindTogglePerspective.isPressed()) {
            //mmm im consuming the keypressess mmmmm
        }
        oldInput.updatePlayerMoveState();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTickEvent(TickEvent.RenderTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        long currTime = System.nanoTime();
        double frameTime = (currTime - lastTime) / 1e9;
        lastTime = currTime;

        MovementInput input = oldInput;
        float forwardImpulse = input.moveForward;
        float leftImpulse = input.moveStrafe;
        int upImpulse = (input.jump ? 1 : 0) + (input.sneak ? -1 : 0);

        forwardVelocity = calculateVelocity(forwardVelocity, forwardImpulse, frameTime);
        leftVelocity = calculateVelocity(leftVelocity, leftImpulse, frameTime);
        upVelocity = calculateVelocity(upVelocity, upImpulse, frameTime);

        Vec3 forward = spectator.getValue() ?
                lookVec : new Vec3(lookVec.xCoord, 0.0, lookVec.zCoord).normalize();
        Vec3 left = new Vec3(lookVec.zCoord, 0.0, -lookVec.xCoord).normalize();

        Vec3 moveDelta = scale(forward, forwardVelocity).add(scale(left, leftVelocity)).add(new Vec3(0.0, upVelocity, 0.0));
        moveDelta = scale(moveDelta, frameTime);
        double speed = Math.sqrt(Math.pow(moveDelta.xCoord, 2) + Math.pow(moveDelta.yCoord, 2) + Math.pow(moveDelta.zCoord, 2)) / frameTime;
        if (speed > MAX_SPEED) {
            double factor = MAX_SPEED / speed;
            forwardVelocity *= factor;
            leftVelocity *= factor;
            upVelocity *= factor;
            moveDelta = scale(moveDelta, factor);
        }
        freeCamPosition.x += moveDelta.xCoord;
        freeCamPosition.y += moveDelta.yCoord;
        freeCamPosition.z += moveDelta.zCoord;
    }


    @ITheAnnotation
    public void onMouseClick(ClickEvent.LeftClick event) {
        event.setCancelled(true);
        if (verbose.getValue()) {
            Managers.NOTIFICATIONS.notify("Camera", "Attempted to click mouse when in Camera mode.", 3000, "other/excl.png");
        }
    }
    @ITheAnnotation
    public void onMouseClick(ClickEvent.RightClick event) {
        event.setCancelled(true);
        if (verbose.getValue()) {
            Managers.NOTIFICATIONS.notify("Camera", "Attempted to click mouse when in Camera mode.", 3000, "other/excl.png");
        }
    }
    @ITheAnnotation
    public void onMouseClick(ClickEvent.MiddleClick event) {
        event.setCancelled(true);
        if (verbose.getValue()) {
            Managers.NOTIFICATIONS.notify("Camera", "Attempted to click mouse when in Camera mode.", 3000, "other/excl.png");
        }
    }

    @ITheAnnotation
    public void onBlockBreak(BlockInteractionEvent.BreakBlock event) {
        event.setCancelled(true);
        if (verbose.getValue()) {
            Managers.NOTIFICATIONS.notify("Camera", "Prevented block breaking in Camera mode.", 3000, "other/excl.png");
        }
    }

    @ITheAnnotation
    public void onBlockPlace(BlockInteractionEvent.PlaceBlock event) {
        event.setCancelled(true);
        if (verbose.getValue()) {
            Managers.NOTIFICATIONS.notify("Camera", "Prevented block placing in Camera mode.", 3000, "other/excl.png");
        }
    }

    @ITheAnnotation
    public void onStartBreaking(BlockInteractionEvent.StartBreaking event) {
        event.setCancelled(true);
        if (verbose.getValue()) {
            Managers.NOTIFICATIONS.notify("Camera", "Prevented block interaction in Camera mode.", 3000, "other/excl.png");
        }
    }

    public void updateCamera() {
        mc.mouseHelper.mouseXYChange();
        float f = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float f1 = f * f * f * 8.0f;
        float f2 = mc.mouseHelper.deltaX * f1;
        float f3 = mc.mouseHelper.deltaY * f1;
        if (mc.gameSettings.invertMouse) {
            f3 *= -1.0f;
        }
        setAngles(f2, f3);
    }

    public static void setAngles(float yaw, float pitch) {
        freeCamPosition.pitch -= pitch * 0.15f;
        freeCamPosition.yaw += yaw * 0.15f;
        freeCamPosition.pitch = MathHelper.clamp_float(freeCamPosition.pitch, -90f, 90f);
        lookVec = getVecFromRotation(freeCamPosition.pitch, freeCamPosition.yaw);
    }

    public void onBeforeRenderWorld() {
        if (!isEnabled()) return;

        if (mc.getRenderViewEntity() != null) {
            Entity entity = mc.getRenderViewEntity();
            shouldOverride = true;
            playerPosition.copyFromEntity(entity, true);
            freeCamPosition.copyToEntity(entity, false);
            oldNoClip = entity.noClip;
            entity.noClip = true;
        }
    }

    public void onAfterRenderWorld() {
        if (!shouldOverride) return;

        MovingObjectPosition looking = mc.objectMouseOver;
        if (mc.getRenderViewEntity() != null) {
            Entity entity = mc.getRenderViewEntity();
            playerPosition.copyToEntity(entity, true);
            shouldOverride = false;
            entity.noClip = oldNoClip;
        }
    }

    public void onBeforeRenderEntity(Entity entity) {
        if (shouldOverride && mc.getRenderViewEntity() == entity) {
            playerPosition.copyToEntity(entity, true);
        }
    }

    public void onAfterRenderEntity(Entity entity) {
        if (shouldOverride && mc.getRenderViewEntity() == entity) {
            freeCamPosition.copyToEntity(entity, false);
        }
    }

    public void onBeforeRenderEntities() {
        entitiesRendering = true;
        if (shouldOverride) {
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    public void onAfterRenderEntities() {
        entitiesRendering = false;
        if (shouldOverride) {
            mc.gameSettings.thirdPersonView = 0;
        }
    }

    public boolean shouldOverrideSpectator(AbstractClientPlayer player) {
        return mc.getRenderViewEntity() == player && shouldOverride && !entitiesRendering;
    }

    private static Vec3 getVecFromRotation(float pitch, float yaw) {
        double f = Math.cos(-yaw * 0.017453292519943295 - Math.PI);
        double f1 = Math.sin(-yaw * 0.017453292519943295 - Math.PI);
        double f2 = -Math.cos(-pitch * 0.017453292519943295);
        double f3 = Math.sin(-pitch * 0.017453292519943295);
        return new Vec3((f1 * f2), f3, (f * f2));
    }

    private static double calculateVelocity(double velocity, double impulse, double frameTime) {
        if (impulse == 0.0) return velocity * Math.pow(SLOWDOWN, frameTime);
        double newVelocity = ACCELERATION * impulse * frameTime;
        if (Math.signum(impulse) == Math.signum(velocity)) newVelocity += velocity;
        return newVelocity;
    }

    private Vec3 scale(Vec3 in, double scale)
    {
        return new Vec3(in.xCoord * scale, in.yCoord * scale, in.zCoord * scale);
    }
}
