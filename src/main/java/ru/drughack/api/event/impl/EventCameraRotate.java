package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.Vec2f;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventCameraRotate extends Event {
    private float yaw;
    private float pitch;

    public void setRotation(Vec2f rotation) {
        yaw = rotation.x;
        pitch = rotation.y;
    }
}