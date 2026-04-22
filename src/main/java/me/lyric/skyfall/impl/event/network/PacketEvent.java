package me.lyric.skyfall.impl.event.network;

import lombok.Getter;
import me.lyric.skyfall.api.event.Event;
import net.minecraft.network.Packet;

@Getter
public class PacketEvent extends Event {
    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }
}
