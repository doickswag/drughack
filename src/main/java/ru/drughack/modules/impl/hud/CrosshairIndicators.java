package ru.drughack.modules.impl.hud;

import net.minecraft.client.util.math.MatrixStack;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.world.BlockUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CrosshairIndicators extends HudModule {

    public CrosshairIndicators() {
        super("CrosshairIndicators", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        float scale = 0.9f;
        float offset = 0;
        List<InfoEntry> entries = new ArrayList<>();
        entries.add(new InfoEntry(DrugHack.getInstance().getProtection().getName().replace(".cc", ""), getColor(ColorType.CLIENT)));
        if (BlockUtils.isHole(BlockUtils.getPlayerPos(true))) entries.add(new InfoEntry("SAFE", getColor(ColorType.CLIENT)));
        if (DrugHack.getInstance().getModuleManager().getAutoCrystal().isToggled()) entries.add(new InfoEntry("CA", getColor(ColorType.CLIENT)));
        if (DrugHack.getInstance().getModuleManager().getFeetTrap().isToggled()) entries.add(new InfoEntry("FT", getColor(ColorType.CLIENT)));

        float maxWidth = 0;
        float textHeight = DrugHack.getInstance().getFontManager().getHeight();

        for (InfoEntry entry : entries) {
            float textWidth = DrugHack.getInstance().getFontManager().getWidth(entry.text);
            maxWidth = Math.max(maxWidth, textWidth);
        }

        for (InfoEntry entry : entries) {
            MatrixStack matrixStack = e.getContext().getMatrices();
            matrixStack.push();
            matrixStack.scale(scale, scale, scale);
            float textWidth = DrugHack.getInstance().getFontManager().getWidth(entry.text);
            float textX = (getX() + ((maxWidth * scale) / 2 - textWidth * scale / 2)) / scale;
            float textY = (getY() + (offset * textHeight)) / scale;
            DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), entry.text, (int) textX, (int) textY, entry.color);
            matrixStack.pop();
            offset++;
        }

        setBounds(getX(), getY(), (int) (maxWidth * scale), (int) (textHeight * entries.size() * scale));
    }

    private enum ColorType {
        RED,
        GREEN,
        WHITE,
        CLIENT
    }

    private Color getColor(ColorType type) {
        if (type == ColorType.RED) {
            return new Color(255, 0, 0);
        } else if (type == ColorType.GREEN) {
            return new Color(47, 173, 26);
        } else if (type == ColorType.WHITE) {
            return new Color(255, 255, 255);
        } else {
            return ColorUtils.getGlobalColor();
        }
    }

    public record InfoEntry(String text, Color color) {}
}