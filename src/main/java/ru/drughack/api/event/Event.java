package ru.drughack.api.event;

import lombok.Getter;

@Getter
public class Event {
    private boolean canceled;

    public void cancel() {
        canceled = true;
    }

    public void resume() {
        canceled = false;
    }
}