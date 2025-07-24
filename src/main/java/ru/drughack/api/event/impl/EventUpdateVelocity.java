package ru.drughack.api.event.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;
import ru.drughack.api.event.Event;

@Getter @Setter @RequiredArgsConstructor
public class EventUpdateVelocity extends Event {
    private final Vec3d movementInput;
    private final float speed;
    private Vec3d velocity;
}