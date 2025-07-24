package ru.drughack.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.api.event.impl.EventPlayerMine;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.impl.player.SpeedMine;
import ru.drughack.modules.settings.Setting;
import net.minecraft.util.math.*;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.other.ThreadExecutor;
import ru.drughack.utils.world.HoleUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.WorldUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blocker extends Module {

    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<Boolean> asynchronous = new Setting<>("Asynchronous",  true);
    public Setting<Integer> bpt = new Setting<>("BPT", 4, 1, 20);
    public Setting<Integer> delay = new Setting<>("Delay", 0, 0, 20);
    public Setting<Float> range = new Setting<>("Range", 5.0f, 0.0f, 12.0f);
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    public Setting<Boolean> crystalDestruction = new Setting<>("CrystalDestruction", true);
    public Setting<Boolean> whileEating = new Setting<>("WhileEating", true);
    public Setting<Boolean> feet = new Setting<>("Feet", true);
    public Setting<Boolean> head = new Setting<>("Head",  true);
    public Setting<Boolean> render = new Setting<>("Render", true);

    private final CopyOnWriteArrayList<Position> targetPositions = new CopyOnWriteArrayList<>();
    private Mine mine = null;

    private int ticks = 0;

    public Blocker() {
        super("Blocker", "block your feettrap", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (!whileEating.getValue() && mc.player.isUsingItem()) return;

        Runnable runnable = () -> {
            if (mc.player == null || mc.world == null) return;

            SpeedMine module = DrugHack.getInstance().getModuleManager().getSpeedMine();
            if (mine != null && (module.getPrimary() != null && mine.position().equals(module.getPrimary().getPosition())) || (module.getSecondary() != null && mine.position().equals(module.getSecondary().getPosition()))) {
                mine = null;
                return;
            }

            int blocksPlaced = 0;
            if (ticks < delay.getValue().intValue()) {
                ticks++;
                return;
            }

            HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions(mc.player, true, false, false);
            List<BlockPos> insidePositions = HoleUtils.getInsidePositions(mc.player);

            if (mine != null && mine.timer().hasTimeElapsed(Math.max(mine.breakTime() - 200L, 0L))) {
                BlockPos position = mine.position();
                if (mine.type() == MineType.FEET && feet.getValue()) {
                    if (feetPositions.contains(mine.position())) {
                        targetPositions.add(new Position(position, position.up()));

                        for (Direction direction : Direction.values()) {
                            if (!direction.getAxis().isHorizontal()) continue;
                            targetPositions.add(new Position(position, position.offset(direction)));
                        }
                    }

                    mine = null;
                } else if ((mine.type() == MineType.HEAD || mine.type() == MineType.SIDE) && head.getValue()) {
                    if ((mine.type() == MineType.HEAD && insidePositions.contains(mine.position().down().down())) || (mine.type() == MineType.SIDE && feetPositions.contains(mine.position().down()))) {
                        targetPositions.add(new Position(position, position.up()));
                    }

                    mine = null;
                }
            }

            targetPositions.removeIf(position -> !WorldUtils.isPlaceable(position.position()));
            targetPositions.removeIf(position -> mc.player.squaredDistanceTo(Vec3d.ofCenter(position.position())) > MathHelper.square(range.getValue().doubleValue()));

            targetPositions.removeIf(position -> !feetPositions.contains(position.original()) && !feetPositions.contains(position.original().down()) && !insidePositions.contains(position.original().down().down()));

            if (targetPositions.isEmpty()) return;

            int slot = InventoryUtils.findHardestBlock(0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
            int previousSlot = mc.player.getInventory().selectedSlot;

            if (slot == -1) {
                targetPositions.clear();
                return;
            }

            InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);

            for (Position position : new ArrayList<>(targetPositions)) {
                if (blocksPlaced >= bpt.getValue().intValue()) break;

                Direction direction = WorldUtils.getDirection(position.position(), null, strictDirection.getValue());
                if (direction == null) continue;

                WorldUtils.placeBlock(position.position(), direction, Hand.MAIN_HAND, rotate.getValue(), crystalDestruction.getValue(), render.getValue());
                targetPositions.remove(position);
                blocksPlaced++;
            }

            InventoryUtils.switchBack(autoSwitch.getValue().name(), slot, previousSlot);

            ticks = 0;
        };

        if (asynchronous.getValue()) ThreadExecutor.execute(runnable);
        else runnable.run();
    }

    @EventHandler
    public void onPlayerMine(EventPlayerMine event) {
        if (mc.player == null || mc.world == null) return;
        if (mine != null && mine.position().equals(event.getPosition())) return;

        SpeedMine module = DrugHack.getInstance().getModuleManager().getSpeedMine();
        if ((module.getPrimary() != null && event.getPosition().equals(module.getPrimary().getPosition())) || (module.getSecondary() != null && event.getPosition().equals(module.getSecondary().getPosition())))
            return;

        Entity entity = mc.world.getEntityById(event.getActorID());

        if (entity == mc.player) return;
        if (!(entity instanceof PlayerEntity player)) return;

        HashSet<BlockPos> feetPositions = HoleUtils.getFeetPositions(mc.player, true, false, false);
        List<BlockPos> insidePositions = HoleUtils.getInsidePositions(mc.player);

        if (feet.getValue() && feetPositions.contains(event.getPosition())) {
            mine = new Mine(event.getPosition(), new TimerUtils(), WorldUtils.getBreakTime(player, mc.world.getBlockState(event.getPosition())), MineType.FEET);
            return;
        }

        if (head.getValue()) {
            if (feetPositions.contains(event.getPosition().down())) {
                mine = new Mine(event.getPosition(), new TimerUtils(), WorldUtils.getBreakTime(player, mc.world.getBlockState(event.getPosition())), MineType.SIDE);
            }

            if (insidePositions.contains(event.getPosition().down().down())) {
                mine = new Mine(event.getPosition(), new TimerUtils(), WorldUtils.getBreakTime(player, mc.world.getBlockState(event.getPosition())), MineType.HEAD);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        targetPositions.clear();
    }

    private record Mine(BlockPos position, TimerUtils timer, float breakTime, MineType type) { }
    private record Position(BlockPos original, BlockPos position) { }

    private enum MineType {
        FEET, HEAD, SIDE
    }
}