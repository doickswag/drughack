package ru.drughack.modules.impl.hud;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.render.ColorUtils;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class TextRadar extends HudModule {

    private final Setting<Integer> limit = new Setting<>("Limit", 10, 0, 20);
    public Map<String, Integer> players = new HashMap<>();
    public int yOffset;

    public TextRadar() {
        super("TextRadar", -1, -1);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        players = getTextRadarMap();
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        yOffset = DrugHack.getInstance().getModuleManager().getWatermark().isToggled() ? 13 : 4;
        float maxWidth = 0f;
        float textHeight = mc.textRenderer.fontHeight + 1;
        int count = 0;
        for (Map.Entry<String, Integer> player : players.entrySet()) {
            String text = player.getKey();
            maxWidth = Math.max(maxWidth, DrugHack.getInstance().getFontManager().getWidth(text));
            count++;
        }
        int y = 0;
        for (Map.Entry<String, Integer> player : players.entrySet()) {
            String text = player.getKey();
            DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), text, (int) getX(), (int) (getY() + y), getColor());
            y += (int) textHeight;
        }

        setBounds(getX(), getY(), (int) maxWidth, (int) (textHeight * count));
    }

    private Color getColor() {
        return ColorUtils.getGlobalColor();
    }

    private Map<String, Integer> getTextRadarMap() {
        Map<String, Integer> players = new HashMap<>();
        DecimalFormat dfDistance = new DecimalFormat("#.#");
        dfDistance.setRoundingMode(RoundingMode.CEILING);
        StringBuilder distanceSB = new StringBuilder();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.isInvisible() || player.getName().equals(mc.player.getName())) continue;
            int distanceInt = (int) mc.player.distanceTo(player);
            String distance = dfDistance.format(distanceInt);
            if (distanceInt >= 25) {
                distanceSB.append(Formatting.GREEN);
            } else if (distanceInt > 10) {
                distanceSB.append(Formatting.YELLOW);
            } else {
                distanceSB.append(Formatting.RED);
            }

            distanceSB.append(distance);
            if (players.size() >= limit.getValue()) continue;
            players.put((getHealthColor(player) + String.valueOf(round2(player.getAbsorptionAmount() + player.getHealth())) + " ") + (DrugHack.getInstance().getFriendManager().contains(player.getName().getString()) ? Formatting.AQUA : Formatting.RESET) + player.getName().getString() + " " + Formatting.WHITE + "[" + Formatting.RESET + distanceSB + "m" + Formatting.WHITE + "] " + Formatting.GREEN, (int) mc.player.distanceTo(player));
            distanceSB.setLength(0);
        }

        if (!players.isEmpty()) players = MathUtils.sortByValue(players, false);
        return players;
    }

    private Formatting getHealthColor(@NotNull PlayerEntity entity) {
        int health = (int) (entity.getHealth() + entity.getAbsorptionAmount());
        if (health <= 15 && health > 7) return Formatting.YELLOW;
        if (health > 15) return Formatting.GREEN;
        return Formatting.RED;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}