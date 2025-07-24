package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventPlayerMine extends Event {
    private final int actorID;
    private final BlockPos position;
}