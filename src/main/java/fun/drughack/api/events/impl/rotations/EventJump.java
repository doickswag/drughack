package fun.drughack.api.events.impl.rotations;

import fun.drughack.api.events.Event;
import lombok.*;

@AllArgsConstructor @Getter @Setter
public class EventJump extends Event {
    private float yaw;
}