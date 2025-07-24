package ru.drughack.api.event.impl;

import lombok.Getter;
import ru.drughack.api.event.Event;
import ru.drughack.modules.settings.Setting;

@Getter
public class EventChangeSetting extends Event {
    private final Setting<?> setting;

    public EventChangeSetting(Setting<?> setting) {
        this.setting = setting;
    }
}