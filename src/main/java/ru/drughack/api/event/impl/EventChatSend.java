package ru.drughack.api.event.impl;

import lombok.*;
import ru.drughack.api.event.Event;

@Getter @Setter @AllArgsConstructor
public class EventChatSend extends Event {
    public String message;
}