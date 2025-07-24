package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.drughack.api.event.Event;

@Getter @Setter @AllArgsConstructor
public class EventSync extends Event {
    private float yaw;
    private float pitch;
}