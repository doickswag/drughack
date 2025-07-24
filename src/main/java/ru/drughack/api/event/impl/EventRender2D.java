package ru.drughack.api.event.impl;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import ru.drughack.api.event.Event;

@Getter
public class EventRender2D extends Event {
    private final DrawContext context;
    private final float delta;

    public EventRender2D(DrawContext context, float delta) {
        this.context = context;
        this.delta = delta;
    }
}