package me.lyric.skyfall.api.utils.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

/**
 * util class representing the position of an entity.
 */
@Getter
@Setter
public class Position {
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    private double lastX;
    private double lastY;
    private double lastZ;
    private double prevX;
    private double prevY;
    private double prevZ;
    private float prevPitch;
    private float prevYaw;

    public Position(double x, double y, double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.prevPitch = pitch;
        this.prevYaw = yaw;
    }

    public Position() {
        this(0.0, 0.0, 0.0, 0.0f, 0.0f);
    }

    private void setDefault() {
        lastX = x;
        lastY = y;
        lastZ = z;
        prevX = x;
        prevY = y;
        prevZ = z;
        prevPitch = pitch;
        prevYaw = yaw;
    }

    public void copyFromEntity(Entity entity, boolean full) {
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
        yaw = entity.rotationYaw;
        pitch = entity.rotationPitch;
        if (full) {
            lastX = entity.lastTickPosX;
            lastY = entity.lastTickPosY;
            lastZ = entity.lastTickPosZ;
            prevX = entity.prevPosX;
            prevY = entity.prevPosY;
            prevZ = entity.prevPosZ;
            prevPitch = entity.prevRotationPitch;
            prevYaw = entity.prevRotationYaw;
        } else {
            setDefault();
        }
    }

    public void copyToEntity(Entity entity, boolean full) {
        entity.setLocationAndAngles(x, y, z, yaw, pitch);
        if (full) {
            entity.lastTickPosX = lastX;
            entity.lastTickPosY = lastY;
            entity.lastTickPosZ = lastZ;
            entity.prevPosX = prevX;
            entity.prevPosY = prevY;
            entity.prevPosZ = prevZ;
            entity.prevRotationPitch = prevPitch;
            entity.prevRotationYaw = prevYaw;
        } else {
            entity.lastTickPosX = x;
            entity.lastTickPosY = y;
            entity.lastTickPosZ = z;
            entity.prevPosX = x;
            entity.prevPosY = y;
            entity.prevPosZ = z;
            entity.prevRotationPitch = pitch;
            entity.prevRotationYaw = yaw;
        }
    }
}
