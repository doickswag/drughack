package ru.drughack.api.event.impl;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import ru.drughack.api.event.Event;
import lombok.*;

@AllArgsConstructor @Getter
public class EventServerConnect extends Event {
    private final ServerAddress address;
    private final ServerInfo info;
}