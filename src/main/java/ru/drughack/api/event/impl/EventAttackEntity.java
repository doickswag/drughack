package ru.drughack.api.event.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import ru.drughack.api.event.Event;
import lombok.*;

@Getter @AllArgsConstructor
public class EventAttackEntity extends Event {
    private final PlayerEntity player;
    private final Entity target;
}