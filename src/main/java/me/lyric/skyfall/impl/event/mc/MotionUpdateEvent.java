package me.lyric.skyfall.impl.event.mc;

import lombok.Getter;
import lombok.Setter;
import me.lyric.skyfall.api.event.Event;

@Getter
@Setter
public class MotionUpdateEvent extends Event {
    double x;
    double y;
    double z;
    double motionX;
    double motionY;
    double motionZ;
    float yaw;
    float pitch;
    boolean onGround;

    public MotionUpdateEvent(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float rotationYaw, float rotationPitch, boolean onGround) {
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = rotationYaw;
        this.pitch = rotationPitch;
        this.onGround = onGround;
    }

    public static class Pre extends MotionUpdateEvent {
        public Pre(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float rotationYaw, float rotationPitch, boolean onGround) {
            super(posX, posY, posZ, motionX, motionY, motionZ, rotationYaw, rotationPitch, onGround);
        }
    }

    public static class Post extends MotionUpdateEvent {
        public Post(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float rotationYaw, float rotationPitch, boolean onGround) {
            super(posX, posY, posZ, motionX, motionY, motionZ, rotationYaw, rotationPitch, onGround);
        }
    }
}
