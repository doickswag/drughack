package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter
public class EventDestroyBlock extends Event {
    private final BlockPos position;
}
