package ru.drughack.utils.world;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;
import ru.drughack.utils.interfaces.Wrapper;

public class MoveUtils implements Wrapper {

    public static double DEFAULT_SPEED = 0.2873;

    public static boolean isMoving() {
        return mc.player.sidewaysSpeed != 0.0f || mc.player.forwardSpeed != 0.0f;
    }

    public static double getPotionSpeed(double speed) {
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) speed *= 1.0 + 0.2 * (mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1);
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) speed /= 1.0 + 0.2 * (mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1);

        return speed;
    }

    public static double getPotionJump(double jump) {
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) jump += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;

        return jump;
    }

    public static Vector2d forward(double speed) {
        float forward = mc.player.input.movementForward;
        float sideways = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (forward == 0.0f && sideways == 0.0f) return new Vector2d(0, 0);
        if (forward != 0.0f) {
            if (sideways >= 1.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
                sideways = 0.0f;
            } else if (sideways <= -1.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
                sideways = 0.0f;
            }

            if (forward > 0.0f) forward = 1.0f;
            else if (forward < 0.0f) forward = -1.0f;
        }

        double motionX = Math.cos(Math.toRadians(yaw + 90.0f));
        double motionZ = Math.sin(Math.toRadians(yaw + 90.0f));

        return new Vector2d(forward * speed * motionX + sideways * speed * motionZ, forward * speed * motionZ - sideways * speed * motionX);
    }

    public static double[] forward$(double speed) {
        float forward = mc.player.input.movementForward;
        float sideways = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (forward == 0.0f && sideways == 0.0f) return new double[]{0, 0};
        if (forward != 0.0f) {
            if (sideways >= 1.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
                sideways = 0.0f;
            } else if (sideways <= -1.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
                sideways = 0.0f;
            }

            if (forward > 0.0f) forward = 1.0f;
            else if (forward < 0.0f) forward = -1.0f;
        }

        double motionX = Math.cos(Math.toRadians(yaw + 90.0f));
        double motionZ = Math.sin(Math.toRadians(yaw + 90.0f));

        return new double[]{forward * speed * motionX + sideways * speed * motionZ, forward * speed * motionZ - sideways * speed * motionX};
    }

    public static double getSpeed() {
        return Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    public static void setSpeed(double speed) {
        double forward = mc.options.forwardKey.isPressed() ? 1.0 : (mc.options.backKey.isPressed() ? -1.0 : 0.0);
        double strafe = mc.options.leftKey.isPressed() ? 1.0 : (mc.options.rightKey.isPressed() ? -1.0 : 0.0);
        float yaw = mc.player.getYaw();

        if (isMoving()) {
            if (forward != 0.0) {
                if (strafe > 0.0) yaw += (float)(forward > 0.0 ? -45 : 45);
                else if (strafe < 0.0) yaw += (float)(forward > 0.0 ? 45 : -45);
                strafe = 0.0;
            }

            double cos = Math.cos(Math.toRadians(yaw + 89.5F));
            double sin = Math.sin(Math.toRadians(yaw + 89.5F));
            mc.player.setVelocity(new Vec3d(forward * speed * cos + strafe * speed * sin, mc.player.getVelocity().y, forward * speed * sin - strafe * speed * cos));
        } else mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
    }
}