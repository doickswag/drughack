package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import net.minecraft.util.math.*;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.other.ThreadExecutor;
import ru.drughack.utils.world.HoleUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.WorldUtils;

import java.util.*;

public class SelfTrap extends Module {

    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Full);
    public Setting<Boolean> head = new Setting<>("Head", true, v -> mode.getValue() == Mode.Full);
    public Setting<Boolean> asynchronous = new Setting<>("Asynchronous",  true);
    public Setting<Integer> bpt = new Setting<>("BPT", 4, 1, 20);
    public Setting<Integer> delay = new Setting<>("Delay", 0, 0, 20);
    public Setting<Boolean> await = new Setting<>("Await", false);
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection",false);
    public Setting<Boolean> crystalDestruction = new Setting<>("CrystalDestruction",  true);
    public Setting<Boolean> antiStep = new Setting<>("AntiStep",  false);
    public Setting<Boolean> antiBomb = new Setting<>("AntiBomb", false);
    public Setting<Boolean> holeCheck = new Setting<>("HoleCheck", false);
    public Setting<Boolean> whileEating = new Setting<>("WhileEating", true);

    public Setting<Boolean> render = new Setting<>("Render", true);

    private List<BlockPos> targetPositions = new ArrayList<>();

    private int ticks = 0;
    private int blocksPlaced = 0;

    public SelfTrap() {
        super("SelfTrap", "trap for self", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (!whileEating.getValue() && mc.player.isUsingItem()) return;

        Runnable runnable = () -> {
            blocksPlaced = 0;
            if (ticks < delay.getValue()) {
                ticks++;
                return;
            }

            if (autoSwitch.getValue() == InventoryUtils.Switch.None && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
                targetPositions = new ArrayList<>();
                return;
            }

            if (holeCheck.getValue() && !HoleUtils.isPlayerInHole(mc.player)) return;

            int slot = InventoryUtils.findHardestBlock(0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
            int previousSlot = mc.player.getInventory().selectedSlot;

            if (slot == -1) {
                targetPositions = new ArrayList<>();
                return;
            }

            targetPositions = HoleUtils.getTrapPositions(mc.player, mode.getValue() == Mode.Partial, head.getValue(), antiStep.getValue(), antiBomb.getValue(), strictDirection.getValue()).stream().filter(WorldUtils::isPlaceable).toList();
            if (targetPositions.isEmpty()) return;
            InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);

            List<BlockPos> placedPositions = new ArrayList<>();
            for (BlockPos position : targetPositions) {
                if (blocksPlaced >= bpt.getValue()) break;

                Direction direction = WorldUtils.getDirection(position, placedPositions, strictDirection.getValue());
                if (direction == null) {
                    BlockPos supportPosition = position.add(0, -1, 0);
                    if (!WorldUtils.isPlaceable(supportPosition)) continue;
                    Direction supportDirection = WorldUtils.getDirection(supportPosition, placedPositions, strictDirection.getValue());
                    if (supportDirection == null) continue;
                    WorldUtils.placeBlock(supportPosition, supportDirection, Hand.MAIN_HAND, rotate.getValue(), crystalDestruction.getValue(), render.getValue());
                    placedPositions.add(supportPosition);
                    blocksPlaced++;
                    if (blocksPlaced >= bpt.getValue()) break;
                    if (await.getValue()) continue;
                    direction = WorldUtils.getDirection(position, placedPositions, strictDirection.getValue());
                    if (direction == null) continue;
                }

                WorldUtils.placeBlock(position, direction, Hand.MAIN_HAND, rotate.getValue(), crystalDestruction.getValue(), render.getValue());
                placedPositions.add(position);
                blocksPlaced++;
            }

            InventoryUtils.switchBack(autoSwitch.getValue().name(), slot, previousSlot);
            ticks = 0;
        };

        if (asynchronous.getValue()) ThreadExecutor.execute(runnable);
        else runnable.run();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null || mc.world == null) toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        targetPositions = new ArrayList<>();
    }

    @Override
    public String getDisplayInfo() {
        if (targetPositions == null) return "0";
        return String.valueOf(targetPositions.size());
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Partial("Partial"),
        Full("Full");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}