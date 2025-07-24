package ru.drughack.utils.render;

import lombok.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.TimerUtils;

import java.awt.*;

@Getter @Setter
public class Particle implements Wrapper {
    private float x, y, width, height, xOffset, yOffset, randomDegrees;
    private final Identifier texture;
    private final Color color;

    public Particle(float x, float y, float width, float height, float xOffset, float yOffset, float randomDegrees, Identifier texture, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.randomDegrees = randomDegrees;
        this.texture = texture;
        this.color = color;
    }

    public void render(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        matrix.push();
        context.getMatrices().translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0);
        matrix.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.toRadians(mc.player.age * 3 + randomDegrees)));
        context.getMatrices().translate(-(getX() + getWidth() / 2f), -(getY() + getHeight() / 2f), 0);
        Renderer2D.renderTexture(matrix, x, y, x + width, y + height, texture, color);
        matrix.pop();
    }

    public void animate() {
        this.x -= xOffset;
        this.y += yOffset;
    }

    public boolean isDead() {
        return y > mc.getWindow().getScaledHeight() || x > mc.getWindow().getScaledWidth() && x < mc.getWindow().getScaledWidth();
    }
}