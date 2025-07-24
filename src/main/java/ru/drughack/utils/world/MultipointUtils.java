package ru.drughack.utils.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.drughack.utils.interfaces.Wrapper;

import java.util.ArrayList;
import java.util.Comparator;

public class MultipointUtils implements Wrapper {

    public static ArrayList<Vec3d> getMultiPoints(Entity entity, int hCount, int vCount) {
        double height = entity.getHeight();
        double width = entity.getWidth();
        double vOffset = height / (vCount + 1);
        double hOffset = width / (hCount + 1);
        ArrayList<Vec3d> list = new ArrayList<>();
        for (int i = 0; i <= vCount + 1; i++) {
            double currentVOffset = vOffset * i;
            for (int j = 0; j <= hCount; j++) {
                double currentHOffset = (width / 2) - hOffset * j;
                list.add(entity.getPos().add(width / 2, currentVOffset, currentHOffset));
                list.add(entity.getPos().add(-currentHOffset, currentVOffset, width / 2));
                list.add(entity.getPos().add(-width / 2, currentVOffset, -currentHOffset));
                list.add(entity.getPos().add(currentHOffset, currentVOffset, -width / 2));
            }
        }

        for (int i = 1; i <= hCount; i++) {
            double offset1 = (width / 2) - hOffset * i;
            for (int j = 1; j <= hCount; j++) {
                double offset2 = (width / 2) - hOffset * j;
                list.add(entity.getPos().add(offset1, 0, offset2));
                list.add(entity.getPos().add(offset1, height, offset2));
            }
        }

        return list;
    }

    public static Vec3d getClosestPoint(Entity entity, int hCount, int vCount, double range, boolean raytrace) {
        Vec3d eyePos = mc.player.getEyePos();
        double rangeSq = range * range;
        return getMultiPoints(entity, hCount, vCount).parallelStream()
                .filter(point -> eyePos.squaredDistanceTo(point) <= rangeSq)
                .sorted(Comparator.comparingDouble(eyePos::squaredDistanceTo))
                .filter(point -> !raytrace || WorldUtils.canSeeOutline(point))
                .findFirst()
                .orElse(Vec3d.ZERO);
    }
}