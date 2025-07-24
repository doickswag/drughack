package ru.drughack.utils.math;

public class TimerUtils {
    private long startTime;

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(Number time) {
        return System.currentTimeMillis() - startTime >= time.longValue();
    }

    public long getPassedTime() {
        return System.currentTimeMillis() - this.startTime;
    }
}