package ru.drughack.managers;

import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventDisconnect;
import ru.drughack.utils.math.TimerUtils;

@Getter
public class ShaderManager {

    private final ShaderTimer timer = new ShaderTimer();

    public ShaderManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    @EventHandler
    public void onDisconnect(EventDisconnect e) {
        timer.reset();
    }

    public void updateTime() {
        timer.update(1);
    }

    @Getter
    public static class ShaderTimer {
        private final TimerUtils timer = new TimerUtils();
        private long passedTime = 0;

        public ShaderTimer() {
            timer.reset();
        }

        public void reset() {
            passedTime = 0;
            timer.reset();
        }

        public void update(float speed) {
            passedTime += (long) (timer.getPassedTime() * speed);
            timer.reset();
        }
    }
}