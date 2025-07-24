package ru.drughack.utils.render;

import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.ColorSetting;

import java.awt.Color;

public class ColorUtils {

    public static int rgb(int r, int g, int b) {
        return  new Color(r, g, b).getRGB();
    }

    public static int rgba(int r, int g, int b, int a) {
        return  new Color(r, g, b, a).getRGB();
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color getGlobalColor() {
        Setting<ColorSetting> color = DrugHack.getInstance().getModuleManager().getUi().color;
        return new Color(color.getValue().getColor().getRed(), color.getValue().getColor().getGreen(), color.getValue().getColor().getBlue());
    }

    public static Color getGlobalColor(int alpha) {
        Setting<ColorSetting> color = DrugHack.getInstance().getModuleManager().getUi().color;
        return new Color(color.getValue().getColor().getRed(), color.getValue().getColor().getGreen(), color.getValue().getColor().getBlue(), alpha);
    }

    public static Formatting getHealthColor(double health) {
        if (health > 18.0) return Formatting.GREEN;
        else if (health > 16.0) return Formatting.DARK_GREEN;
        else if (health > 12.0) return Formatting.YELLOW;
        else if (health > 8.0) return Formatting.GOLD;
        else if (health > 5.0) return Formatting.RED;

        return Formatting.DARK_RED;
    }

    public static Formatting getTotemColor(int pops) {
        if (pops == 1) return Formatting.GREEN;
        else if (pops == 2) return Formatting.DARK_GREEN;
        else if (pops == 3) return Formatting.YELLOW;
        else if (pops == 4) return Formatting.GOLD;
        else if (pops == 5) return Formatting.RED;

        return Formatting.DARK_RED;
    }

    public static Color getPulse(Color color) {
        return getPulse(color, 15);
    }

    public static Color getPulse(Color color, long speed) {
        speed = Math.max(1, Math.min(speed, 20));
        double sin = Math.sin(2 * Math.PI * (speed / 20f) * ((System.currentTimeMillis() - DrugHack.initTime) / 1000f));
        double scale = ((sin + 1) / 2) * (color.getAlpha() - 25) + 25;
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) scale);
    }

    public static Color getRainbow() {
        return getRainbow(255);
    }

    public static Color getRainbow(int alpha) {
        return getRainbow(6L, 1f, 1f, alpha);
    }

    public static Color getRainbow(long speed, float saturation, float brightness, int alpha) {
        return getRainbow(speed, saturation, brightness, alpha, 0);
    }

    public static Color getRainbow(long speed, float saturation, float brightness, int alpha, long index) {
        speed = Math.clamp(speed, 1, 20);
        float hue = ((System.currentTimeMillis() + index) % (10500 - (500 * speed))) / (10500.0f - (500.0f * (float) speed));
        Color color = new Color(Color.HSBtoRGB(Math.clamp(hue, 0.0f, 1.0f), Math.clamp(saturation, 0.0f, 1.0f), Math.clamp(brightness, 0.0f, 1.0f)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}