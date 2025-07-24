package ru.drughack.modules.impl.hud;

import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.world.WorldUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Coordinates extends HudModule {

    private final Setting<Boolean> direction = new Setting<>("Direction", true);
    private final Setting<Boolean> nether = new Setting<>("NetherCoords", false);

    public Coordinates() {
        super("Coordinates", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        float textHeight = DrugHack.getInstance().getFontManager().getHeight();
        float totalWidth, totalHeight;

        List<String> entries = new ArrayList<>();

        String coordinates = CustomFormatting.CLIENT + String.valueOf(mc.player.getBlockX()) +
                (nether.getValue() ? (CustomFormatting.CLIENT + " [" + CustomFormatting.CLIENT + WorldUtils.getNetherPosition(mc.player.getBlockX()) + CustomFormatting.CLIENT + "]") : "") +
                CustomFormatting.CLIENT + ", " + CustomFormatting.CLIENT + mc.player.getBlockY() +
                CustomFormatting.CLIENT + ", " + CustomFormatting.CLIENT + mc.player.getBlockZ() +
                (nether.getValue() ? (CustomFormatting.CLIENT + " [" + CustomFormatting.CLIENT + WorldUtils.getNetherPosition(mc.player.getBlockZ()) + CustomFormatting.CLIENT + "]") : "");

        entries.add(coordinates);

        if (direction.getValue()) {
            String direction = CustomFormatting.CLIENT + StringUtils.capitalize(mc.player.getMovementDirection().getName()) +
                    Formatting.WHITE + " [" + CustomFormatting.CLIENT +
                    WorldUtils.getMovementDirection(mc.player.getMovementDirection()) + Formatting.WHITE + "]";

            entries.add(direction);
        }

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
            boolean isDirection = text.contains("[" + WorldUtils.getMovementDirection(mc.player.getMovementDirection()) + "]");
            boolean isRight = mc.getWindow().getScaledWidth() / 2f < getX() + totalWidth - DrugHack.getInstance().getFontManager().getWidth(text);
            float x = isRight ? getX() + totalWidth - DrugHack.getInstance().getFontManager().getWidth(text) : getX();
            DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), text, (int) x, (int) (getY() + offset * textHeight), isDirection ? Color.WHITE : ColorUtils.getGlobalColor());
            offset++;
        }

        setBounds(getX(), getY(), (int) totalWidth, (int) totalHeight);
    }
}
