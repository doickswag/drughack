package fun.drughack.api.events.impl;

import fun.drughack.api.events.Event;
import fun.drughack.modules.settings.Setting;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class EventSettingChange extends Event {
    private final Setting<?> setting;
}