package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter
public class EventTargetDeath extends Event {
    private final PlayerEntity player;
}