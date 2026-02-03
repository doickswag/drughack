package fun.drughack.api.events.impl.rotations;

import fun.drughack.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class EventTravel extends Event {
    private float yaw, pitch;
}