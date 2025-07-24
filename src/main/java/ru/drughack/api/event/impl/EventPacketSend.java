package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.packet.Packet;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventPacketSend extends Event {
    private final Packet<?> packet;

    @Getter @AllArgsConstructor
    public static class Post extends Event {
        private final Packet<?> packet;
    }
}