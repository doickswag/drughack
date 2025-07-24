package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import ru.drughack.api.event.Event;

@AllArgsConstructor @Getter @Setter
public class EventMotion extends Event {
    private final MovementType movementType;
    private Vec3d movement;

    public double getX() {
        return movement.getX();
    }
    public double getY() {
        return movement.getY();
    }
    public double getZ() {
        return movement.getZ();
    }

    public void setX(double x) {
        this.movement = new Vec3d(x, movement.getY(), movement.getZ());
    }
    public void setY(double y) {
        this.movement = new Vec3d(movement.getX(), y, movement.getZ());
    }
    public void setZ(double z) {
        this.movement = new Vec3d(movement.getX(), movement.getY(), z);
    }
}