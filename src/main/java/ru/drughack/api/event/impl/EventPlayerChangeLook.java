package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventPlayerChangeLook extends Event {
    private double cursorDeltaX, cursorDeltaY;

    public static class Post extends EventPlayerChangeLook {
        public Post(double cursorDeltaX, double cursorDeltaY) {
            super(cursorDeltaX, cursorDeltaY);
        }
    }

    public static class Pre {}
}