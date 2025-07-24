package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter @Setter
public class EventKeyboardTick extends Event {
    private float movementForward;
    private float movementSideways;
}