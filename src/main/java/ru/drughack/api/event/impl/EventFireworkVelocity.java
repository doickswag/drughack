package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter @Setter
public class EventFireworkVelocity extends Event {
    private Vec3d velocity;
}