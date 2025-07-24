package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter
public class EventEntitySpawn extends Event {
    private final Entity entity;
}