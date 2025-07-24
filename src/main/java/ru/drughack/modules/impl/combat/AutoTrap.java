package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.other.ThreadExecutor;
import ru.drughack.utils.world.HoleUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.WorldUtils;

import java.util.*;

public class AutoTrap extends Module {

    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Full);
    public Setting<Boolean> head = new Setting<>("Head", true, v -> mode.getValue() == Mode.Full);
    public Setting<Boolean> asynchronous = new Setting<>("Asynchronous", true);
    public Setting<Integer> bpt = new Setting<>("BPT", 4, 1, 20);
    public Setting<Integer> delay = new Setting<>("Delay", 0, 0, 20);
    public Setting<Float> range = new Setting<>("Range", 5f, 0f, 12f);
    public Setting<Float> enemyRange = new Setting<>("EnemyRange", 8.0f, 0.0f, 16.0f);
    public Setting<Boolean> await = new Setting<>("Await", false);
    public Setting<Boolean> rotate = new Setting<>("Rotate",  true);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    public Setting<Boolean> crystalDestruction = new Setting<>("CrystalDestruction", true);
    public Setting<Boolean> holeCheck = new Setting<>("HoleCheck", true);
    public Setting<Boolean> antiStep = new Setting<>("AntiStep", false);
    public Setting<Boolean> whileEating = new Setting<>("WhileEating", true);
    public Setting<Boolean> render = new Setting<>("Render", true);

    private List<BlockPos> positions = new ArrayList<>();

    private int ticks = 0;
    private int blocksPlaced = 0;

    @Override
    public String getDisplayInfo() {
        return String.valueOf(positions.size());
    }

    public AutoTrap() {
        super("AutoTrap", "trap", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (!whileEating.getValue() && mc.player.isUsingItem()) return;

        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();

        Runnable runnable = () -> {
            blocksPlaced = 0;
            if (ticks < delay.getValue()) {
                ticks++;
                return;
            }

            if (autoSwitch.getValue() == InventoryUtils.Switch.None && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
                positions = new ArrayList<>();
                return;
            }

            int slot = InventoryUtils.findHardestBlock(0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
            int previousSlot = mc.player.getInventory().selectedSlot;

            if (slot == -1) {
                positions = new ArrayList<>();
                return;
            }

            Target target = getTarget(players);
            if (target == null) {
                positions = new ArrayList<>();
                return;
            }

            positions = target.positions();

            InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);

            List<BlockPos> placedPositions = new ArrayList<>();
            for (BlockPos position : positions) {
                if (blocksPlaced >= bpt.getValue().intValue()) break;

                Direction direction = WorldUtils.getDirection(position, placedPositions, strictDirection.getValue());
                if (direction == null) {
                    BlockPos supportPosition = position.add(0, position.getY() - target.player().getBlockY() == 1 ? 1 : -1, 0);
                    if (!WorldUtils.isPlaceable(supportPosition)) continue;

                    Direction supportDirection = WorldUtils.getDirection(supportPosition, placedPositions, strictDirection.getValue());
                    if (supportDirection == null) continue;

                    WorldUtils.placeBlock(supportPosition, supportDirection, Hand.MAIN_HAND, rotate.getValue(), crystalDestruction.getValue(), render.getValue());
                    placedPositions.add(supportPosition);
                    blocksPlaced++;

                    if (blocksPlaced >= bpt.getValue().intValue()) break;
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

    private Target getTarget(List<AbstractClientPlayerEntity> players) {
        Target optimalTarget = null;
        for (PlayerEntity player : players) {
            if (player == mc.player) continue;
            if (!player.isAlive() || player.getHealth() <= 0.0f) continue;
            if (mc.player.squaredDistanceTo(player) > MathHelper.square(enemyRange.getValue().doubleValue())) continue;
            if (DrugHack.getInstance().getFriendManager().contains(player.getName().getString())) continue;
            if (holeCheck.getValue() && !HoleUtils.isPlayerInHole(player)) continue;

            List<BlockPos> positions = HoleUtils.getTrapPositions(player, mode.getValue() == Mode.Partial, head.getValue(), antiStep.getValue(), false, strictDirection.getValue()).stream()
                    .filter(position -> mc.player.squaredDistanceTo(Vec3d.ofCenter(position)) <= MathHelper.square(range.getValue().doubleValue()))
                    .filter(WorldUtils::isPlaceable)
                    .toList();

            if (positions.isEmpty()) continue;

            if (optimalTarget == null) {
                optimalTarget = new Target(player, positions);
                continue;
            }

            if (mc.player.squaredDistanceTo(player) < mc.player.squaredDistanceTo(optimalTarget.player())) {
                optimalTarget = new Target(player, positions);
            }
        }

        return optimalTarget;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null || mc.world == null) toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        positions = new ArrayList<>();
    }

    private record Target(PlayerEntity player, List<BlockPos> positions) { }

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