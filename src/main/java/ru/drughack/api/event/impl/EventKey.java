package ru.drughack.api.event.impl;

import lombok.Getter;
import ru.drughack.api.event.Event;

@Getter
public class EventKey extends Event {

    private final int key;
    private final int action;

    public EventKey(int key, int action) {
        this.key = key;
        this.action = action;
    }
}