package ru.drughack.api.event.impl;

import lombok.Getter;
import ru.drughack.api.event.Event;

@Getter
public class EventMouse extends Event {
    int button;
    int action;

    public EventMouse(int button, int action) {
        this.button = button;
        this.action = action;
    }
}