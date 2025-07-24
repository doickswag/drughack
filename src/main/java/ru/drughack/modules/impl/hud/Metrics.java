package ru.drughack.modules.impl.hud;

import lombok.AllArgsConstructor;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.world.EntityUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Metrics extends HudModule {

    private final Setting<Mode> speed = new Setting<>("Speed", Mode.Meters);

    public Metrics() {
        super("Metrics", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        float textHeight = DrugHack.getInstance().getFontManager().getHeight();
        float tickRate = DrugHack.getInstance().getServerManager().getTickRate();
        float totalWidth, totalHeight;

        List<String> entries = new ArrayList<>();

        entries.add(CustomFormatting.CLIENT + "Ping " + Formatting.WHITE + DrugHack.getInstance().getServerManager().getPing() + "ms");
        entries.add(CustomFormatting.CLIENT + "FPS " + Formatting.WHITE + DrugHack.getInstance().getRenderManager().getFps());
        if (mc.player.getMainHandStack().isDamageable()) entries.add(CustomFormatting.CLIENT + "Durability " + Formatting.RESET + (mc.player.getMainHandStack().getMaxDamage() - mc.player.getMainHandStack().getDamage()));
        entries.add(CustomFormatting.CLIENT + "Speed " + Formatting.WHITE + new DecimalFormat("0.00").format(EntityUtils.getSpeed(mc.player, speed.getValue() == Mode.Meters ? EntityUtils.SpeedUnit.METERS : EntityUtils.SpeedUnit.KILOMETERS)) + (speed.getValue() == Mode.Meters ? "m/s" : "km/h"));
        entries.add(CustomFormatting.CLIENT + "Brand " + Formatting.WHITE + DrugHack.getInstance().getServerManager().getServerBrand());
        entries.add(CustomFormatting.CLIENT + "TPS " + Formatting.WHITE + (tickRate > 19.79 ? "20.00" : new DecimalFormat("00.00").format(tickRate)));

        entries.sort(Comparator.comparingInt(DrugHack.getInstance().getFontManager()::getWidth));

        totalWidth = 0f;
        totalHeight = 0f;
        for (String text : entries) {
            float textWidth = DrugHack.getInstance().getFontManager().getWidth(text);
            if (textWidth > totalWidth) totalWidth = textWidth;
            totalHeight += textHeight;
        }

        float offset = 0;
        for (String text : entries) {
            if (text.startsWith("Durability") && mc.player.getMainHandStack().isDamageable()) {
                int maxDamage = mc.player.getMainHandStack().getMaxDamage();
                int damage = mc.player.getMainHandStack().getDamage();
                String s = String.valueOf(maxDamage - damage);
                float durabilityX = getX() + totalWidth - DrugHack.getInstance().getFontManager().getWidth("Durability ") - DrugHack.getInstance().getFontManager().getWidth(s);
                DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), Formatting.WHITE + "Durability ", (int) durabilityX, (int) (getY() + offset * textHeight), ColorUtils.getGlobalColor());
                DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), s, (int) (getX() + totalWidth - DrugHack.getInstance().getFontManager().getWidth(s)), (int) (getY() + offset * textHeight), new Color(1.0f - ((maxDamage - damage) / (float) maxDamage), (maxDamage - damage) / (float) maxDamage, 0));
            } else {
                boolean isRight = mc.getWindow().getScaledWidth() / 2f < getX() + totalWidth - DrugHack.getInstance().getFontManager().getWidth(text);
                float x = isRight ? getX() + totalWidth - DrugHack.getInstance().getFontManager().getWidth(text) : getX();
                DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), text, (int) x, (int) (getY() + offset * textHeight), Color.WHITE);
            }

            offset++;
        }

        setBounds(getX(), getY(), (int) totalWidth, (int) totalHeight);
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Kilometers("Kilometers"),
        Meters("Meters");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}