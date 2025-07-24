package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter
public class EventAttackBlock extends Event {
    private final BlockPos position;
    private final Direction direction;
}