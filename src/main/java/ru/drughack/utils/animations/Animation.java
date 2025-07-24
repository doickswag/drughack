package ru.drughack.utils.animations;

import lombok.*;
import ru.drughack.utils.interfaces.Wrapper;

@Getter
@Setter
public class Animation implements Wrapper, Cloneable {
    private final Easing easing;
    private int millis;
    private long startMillis;

    public Animation(Easing easing, int millis) {
        this.easing = easing;
        this.millis = millis;
        startMillis = System.currentTimeMillis();
    }

    public void reset() {
        startMillis = System.currentTimeMillis();
    }

    public double getEase() {
        long currentMillis = getPassedMillis();
        return currentMillis >= millis ? 1 : easing.ease(currentMillis / (double) millis);
    }

    public long getPassedMillis() {
        return System.currentTimeMillis() - startMillis;
    }

    @Override
    public Animation clone() {
        return new Animation(easing, millis);
    }
}