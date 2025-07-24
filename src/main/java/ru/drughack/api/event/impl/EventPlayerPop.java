package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventPlayerPop extends Event {
    private final PlayerEntity player;
    private final int pops;
}