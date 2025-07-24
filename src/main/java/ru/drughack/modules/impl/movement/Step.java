package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventMoveUpdate;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class Step extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.VANILLA);
    public Setting<Float> height = new Setting<>("Height", 2.0f, 0.0f, 12.0f);
    public Setting<Boolean> useTimer = new Setting<>("UseTimer", false, v -> mode.getValue() == Mode.NCP);

    private boolean resetTimer = false;

    public Step() {
        super("Step", "Step", Category.Movement);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (resetTimer) {
            DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f);
            resetTimer = false;
        }
    }

    @EventHandler
    public void onUpdateMovement(EventMoveUpdate event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mode.getValue() == Mode.NCP)) return;

        double stepHeight = mc.player.getY() - mc.player.prevY;
        if (stepHeight <= 0.75 || stepHeight > height.getValue().doubleValue())
            return;

        double[] offsets = getOffset(stepHeight);
        if (offsets != null && offsets.length > 1) {
            if (useTimer.getValue()) {
                DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f / offsets.length);
                resetTimer = true;
            }

            for (double offset : offsets) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.prevX, mc.player.prevY + offset, mc.player.prevZ, false, mc.player.horizontalCollision));
            }
        }
    }

    public double[] getOffset(double height) {
        return switch ((int) (height * 10000)) {
            case 7500, 10000 -> new double[]{0.42, 0.753};
            case 8125, 8750 -> new double[]{0.39, 0.7};
            case 15000 -> new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
            case 20000 -> new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
            case 250000 -> new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
            default -> null;
        };
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        NCP("NCP"),
        VANILLA("Vanilla");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}