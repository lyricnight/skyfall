package me.lyric.skyfall.asm.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.impl.event.network.PacketEvent;
import me.lyric.skyfall.impl.manager.Server;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author lyric
 */
@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager extends SimpleChannelInboundHandler<Packet<?>> {
    @Shadow
    @Final
    private EnumPacketDirection direction;

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void channelRead0Hook(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci)
    {
        if (direction == EnumPacketDirection.CLIENTBOUND)
        {
            PacketEvent.Receive event = new PacketEvent.Receive(p_channelRead0_2_);
            EventBus.getInstance().post(event);
            if (event.isCancelled())
            {
                ci.cancel();
            }
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!Managers.SERVER.isSilent(packet))
        {
            PacketEvent.Send event = new PacketEvent.Send(packet);
            EventBus.getInstance().post(event);
            if (event.isCancelled()) ci.cancel();
        }
    }
}
