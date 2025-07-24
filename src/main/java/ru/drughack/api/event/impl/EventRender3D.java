package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventRender3D extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;

    @Getter @AllArgsConstructor
    public static class Post extends Event {
        private final MatrixStack matrices;
        private final float tickDelta;
    }
}