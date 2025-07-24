package ru.drughack.utils.mixins;

import net.minecraft.network.packet.Packet;

public interface IClientConnection {

    void sendPacket(Packet<?> packet);
}