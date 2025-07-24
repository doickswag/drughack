package ru.drughack.api.mixins;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventDisconnect;
import ru.drughack.api.event.impl.EventPacket;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.event.impl.EventPacketSend;
import ru.drughack.utils.mixins.IClientConnection;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements IClientConnection {

    @Shadow private int packetsSentCounter;
    @Shadow private Channel channel;
    @Shadow protected abstract void sendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void send$HEAD(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo info) {
        EventPacketSend event = new EventPacketSend(packet);
        EventPacket eventV2 = new EventPacket(packet);
        DrugHack.getInstance().getEventHandler().post(event);
        DrugHack.getInstance().getEventHandler().post(eventV2);
        if (event.isCanceled()) info.cancel();
        if (eventV2.isCanceled()) info.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("TAIL"), cancellable = true)
    private void send$TAIL(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo info) {
        DrugHack.getInstance().getEventHandler().post(new EventPacketSend.Post(packet));
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo info) {
        EventPacketReceive event = new EventPacketReceive(packet);
        EventPacket eventV2 = new EventPacket(packet);
        DrugHack.getInstance().getEventHandler().post(event);
        DrugHack.getInstance().getEventHandler().post(eventV2);
        if (packet instanceof BundleS2CPacket bundleS2CPacket) for (Packet<?> subPacket : bundleS2CPacket.getPackets()) DrugHack.getInstance().getEventHandler().post(new EventPacketReceive(subPacket));
        if (event.isCanceled()) info.cancel();
        if (eventV2.isCanceled()) info.cancel();
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        ++packetsSentCounter;
        if (channel.eventLoop().inEventLoop()) sendInternal(packet, null, false);
        else channel.eventLoop().execute(() -> sendInternal(packet, null, false));
    }

    @Inject(method = "disconnect*", at = @At(value = "HEAD"))
    private void hookDisconnect(Text disconnectReason, CallbackInfo ci) {
        DrugHack.getInstance().getEventHandler().post(new EventDisconnect());
    }
}