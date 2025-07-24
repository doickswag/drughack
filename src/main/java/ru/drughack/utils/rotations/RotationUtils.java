package ru.drughack.utils.rotations;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.MathUtils;

public class RotationUtils implements Wrapper {
    public static float[] getRotations(Entity entity) {
        return getRotations(entity.getX(), entity.getY(), entity.getZ());
    }

    public static float[] getRotations(Vec3d vec3d) {
        return getRotations(vec3d.x, vec3d.y, vec3d.z);
    }

    public static float[] getRotations(double x, double y, double z) {
        double deltaX = x - mc.player.getX();
        double deltaY = y - mc.player.getEyeY();
        double deltaZ = z - mc.player.getZ();
        double distance = MathHelper.sqrt((float)(deltaX * deltaX + deltaZ * deltaZ));

        float yaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180D / Math.PI) - 90.0F) + (float) MathUtils.random(1.0D, 0.0D);
        float pitch = (float) (-MathHelper.atan2(deltaY, distance) * (180D / Math.PI)) + (float) MathUtils.random(1.0D, 0.0D);
        return new float[]{yaw, pitch};
    }

    public static Vec3d getVecRotations(float yaw, float pitch) {
        float f = pitch * ((float) Math.PI / 180F);
        float f1 = -yaw * ((float) Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vec3d(f3 * f4, -f5, f2 * f4);
    }

    public static float[] getRotations(Direction direction) {
        return switch (direction) {
            case DOWN -> new float[]{mc.player.getYaw(), 90.0f};
            case UP -> new float[]{mc.player.getYaw(), -90.0f};
            case NORTH -> new float[]{180.0f, mc.player.getPitch()};
            case SOUTH -> new float[]{0.0f, mc.player.getPitch()};
            case WEST -> new float[]{90.0f, mc.player.getPitch()};
            case EAST -> new float[]{-90.0f, mc.player.getPitch()};
        };
    }
}