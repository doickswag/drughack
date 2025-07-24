package ru.drughack.modules.impl.hud;

import net.minecraft.item.Items;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.managers.ModuleManager;
import ru.drughack.modules.impl.combat.Aura;
import ru.drughack.modules.impl.combat.AutoCrystal;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.world.BlockUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Indicators extends HudModule {

    private final Setting<Boolean> caps = new Setting<>("Caps", true);

    public Indicators() {
        super("Indicators", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        float offset = 0;
        int ping = DrugHack.getInstance().getServerManager().getPing();
        int totems = mc.player.getInventory().count(Items.TOTEM_OF_UNDYING);
        float maxWidth = 0;
        float textHeight = DrugHack.getInstance().getFontManager().getHeight();

        List<InfoEntry> entries = new ArrayList<>();
        ModuleManager module = DrugHack.getInstance().getModuleManager();
        AutoCrystal ca = module.getAutoCrystal();
        Aura ka = module.getAura();

        entries.add(new InfoEntry(DrugHack.getInstance().getProtection().getName(), getColor(ColorType.CLIENT)));

        if (ka.getTarget() != null && mc.player.distanceTo(ka.getTarget()) < ka.getRange()) {
            entries.add(new InfoEntry("HTR", getColor(ColorType.RED)));
        } else {
            entries.add(new InfoEntry("HTR", getColor(ColorType.GREEN)));
        }

        if (ca.getTarget() != null && mc.player.distanceTo(ca.getTarget()) < ca.enemyRange.getValue()) {
            entries.add(new InfoEntry("PLR", getColor(ColorType.RED)));
        } else {
            entries.add(new InfoEntry("PLR", getColor(ColorType.GREEN)));
        }

        if (BlockUtils.isHole(BlockUtils.getPlayerPos(true))) {
            entries.add(new InfoEntry("SAFE", getColor(ColorType.GREEN)));
        } else {
            entries.add(new InfoEntry("SAFE", getColor(ColorType.RED)));
        }

        if (module.getSpeed().isToggled()) entries.add(new InfoEntry("BHOP", getColor(ColorType.CLIENT)));
        if (module.getSpeedMine().isToggled()
                && module.getSpeedMine().getPrimary() != null
                && module.getSpeedMine().getPrimary().getSpeed() > 0
                || (module.getSpeedMine().getSecondary() != null
                && module.getSpeedMine().getSecondary().getSpeed() > 0))

            entries.add(new InfoEntry("MINING " + module.getSpeedMine().getPrimary().getSpeed() + (module.getSpeedMine().getSecondary() == null ? "" : ", " + module.getSpeedMine().getSecondary().getSpeed()), getColor(ColorType.CLIENT)));

        if (module.getAutoTrap().isToggled()) entries.add(new InfoEntry("AT", getColor(ColorType.CLIENT)));
        if (module.getSelfTrap().isToggled()) entries.add(new InfoEntry("SF", getColor(ColorType.CLIENT)));
        if (module.getFeetTrap().isToggled()) entries.add(new InfoEntry("FT", getColor(ColorType.CLIENT)));

        if (ping < 100) {
            entries.add(new InfoEntry("PING " + ping, getColor(ColorType.GREEN)));
        } else {
            entries.add(new InfoEntry("PING " + ping, getColor(ColorType.RED)));
        }

        if (totems > 0) entries.add(new InfoEntry("TOTEMS " + totems, getColor(ColorType.GREEN)));
        else entries.add(new InfoEntry("TOTEMS " + totems, getColor(ColorType.RED)));

        for (InfoEntry entry : entries) {
            float textWidth = DrugHack.getInstance().getFontManager().getWidth(entry.text());
            maxWidth = Math.max(maxWidth, textWidth);
        }

        for (InfoEntry entry : entries) {
            float textY = getY() + (offset * textHeight);
            boolean isRight = mc.getWindow().getScaledWidth() / 2f < getX() + maxWidth - DrugHack.getInstance().getFontManager().getWidth(entry.text());
            float x = isRight ? getX() + maxWidth - DrugHack.getInstance().getFontManager().getWidth(entry.text()) : getX();
            DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), caps.getValue() ? entry.text : entry.text.toLowerCase(), (int) x, (int) textY, entry.color());
            offset++;
        }

        setBounds(getX(), getY(), (int) maxWidth, (int) (textHeight * entries.size()));
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