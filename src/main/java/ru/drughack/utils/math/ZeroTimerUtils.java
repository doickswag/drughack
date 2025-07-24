package ru.drughack.utils.math;

public class ZeroTimerUtils {
    private long startTime;

    public ZeroTimerUtils() {
        startTime = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long time) {
        if (startTime == 0L) return true;
        return System.currentTimeMillis() - startTime >= time;
    }

    public void zero() {
        startTime = 0L;
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }
}