package ru.drughack.utils.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static ru.drughack.utils.interfaces.Wrapper.mc;

public class MathUtils {

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double random(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * clamp(deltaTime() * multiple, 0, 1));
    }

    public static double deltaTime() {
        return mc.getCurrentFps() > 0 ? (1.0000 / mc.getCurrentFps()) : 1;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
        LinkedList<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        if (descending) list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        else list.sort(Map.Entry.comparingByValue());

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) result.put(entry.getKey(), entry.getValue());
        return result;
    }

    public static double interpolate(double value, double newValue, double interpolation) {
        return (value + (newValue - value) * interpolation);
    }

    public static float lerp$(float start, float end, float t) {
        t = Math.max(0, Math.min(1, t));
        return start + (end - start) * t;
    }

    public static double roundNumber(double number, int scale) {
        BigDecimal decimal = new BigDecimal(number);
        decimal = decimal.setScale(scale, RoundingMode.HALF_UP);

        return decimal.doubleValue();
    }

    public static Vec3d transformPos(Matrix4f matrix, float x, float y, float z) {
        Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
        return new Vec3d(vector3f.x(), vector3f.y(), vector3f.z());
    }
}