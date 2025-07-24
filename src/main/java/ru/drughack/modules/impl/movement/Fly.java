package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import ru.drughack.api.event.impl.EventMotion;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class Fly extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);

    public Fly() {
        super("Fly", "simulate fly without creative", Category.Movement);
    }

    private double moveSpeed;
    private double lastDist;
    private int stage;

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.NCP) {
            mc.player.setOnGround(true);
            if (mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F) {
                double xDist = mc.player.getX() - mc.player.prevX;
                double zDist = mc.player.getZ() - mc.player.prevZ;
                this.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
            }
        }
    }

    @EventHandler
    public void onMove(EventMotion e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.NCP) {
            if (mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F) {
                if (stage == 0) this.moveSpeed = 3.8D + getBaseMoveSpeed() - 0.05D;
                else if (stage == 1) {
                    e.setY(e.getY() + 0.42D);
                    this.moveSpeed *= 2.13D;
                } else if (stage == 2) {
                    double difference = 0.66D * (this.lastDist - getBaseMoveSpeed());
                    this.moveSpeed = this.lastDist - difference;
                } else this.moveSpeed = this.lastDist - this.lastDist / 159.0D;

                setMoveSpeed(e, this.moveSpeed = Math.max(getBaseMoveSpeed(), this.moveSpeed));
                ++stage;
            }
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.NCP) {
            this.moveSpeed = getBaseMoveSpeed();
            this.lastDist = 0.0D;
            stage = 0;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0D + 0.2D * (double)(amplifier + 1);
        }

        return baseSpeed;
    }

    private void setMoveSpeed(EventMotion event, double speed) {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }

                strafe = 0.0;

                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw + 90.0f));
            double sin = Math.sin(Math.toRadians(yaw + 90.0f));
            event.setX(forward * speed * cos + strafe * speed * sin);
            event.setZ(forward * speed * sin - strafe * speed * cos);
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        NCP("NCP");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}