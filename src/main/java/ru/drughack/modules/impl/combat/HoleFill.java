package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
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
import ru.drughack.utils.world.PositionUtils;
import ru.drughack.utils.world.WorldUtils;

import java.util.ArrayList;
import java.util.List;

public class HoleFill extends Module {

    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    public Setting<Boolean> webs = new Setting<>("Webs", false);
    public Setting<Boolean> selfWeb = new  Setting<>("SelfWeb", false);
    public Setting<Boolean> asynchronous = new  Setting<>("Asynchronous", true);
    public Setting<Integer> bpt = new Setting<>("BPT", 1, 1, 20);
    public Setting<Integer> delay = new Setting<>("Delay", 0, 0, 20);
    public Setting<Float> range = new Setting<>("Range", 5.0f, 0.0f, 12.0f);
    public Setting<Float> enemyRange = new Setting<>("EnemyRange", 8.0f, 0.0f, 16.0f, v -> mode.getValue() == Mode.Smart);
    public Setting<Float> smartRange = new Setting<>("SmartRange", 3.0f, 0.0f, 6.0f, v -> mode.getValue() == Mode.Smart);
    public Setting<Boolean> safety = new  Setting<>("Safety", true);
    public Setting<Float> safetyRange = new Setting<>("SafetyRange", 2.0f, 0.0f, 6.0f, v -> safety.getValue());
    public Setting<Boolean> rotate = new  Setting<>("Rotate", true);
    public Setting<Boolean> strictDirection = new  Setting<>("StrictDirection", false);
    public Setting<Boolean> crystalDestruction = new  Setting<>("CrystalDestruction", true);
    public Setting<Boolean> doubleHoles = new  Setting<>("DoubleHoles", false);
    public Setting<Boolean> holeCheck = new  Setting<>("HoleCheck", true);
    public Setting<Boolean> whileEating = new  Setting<>("WhileEating", true);
    public Setting<Boolean> render = new  Setting<>("Render", true);

    private List<BlockPos> positions = new ArrayList<>();

    private int ticks = 0;
    private int blocksPlaced = 0;

    public HoleFill() {
        super("HoleFill", "fill hole", Category.Combat);
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

            boolean flag = webs.getValue() ? !mc.player.getMainHandStack().getItem().equals(Items.COBWEB) : !(mc.player.getMainHandStack().getItem() instanceof BlockItem);
            if (autoSwitch.getValue()== InventoryUtils.Switch.None && flag) {
                positions = new ArrayList<>();
                return;
            }
            int slot;
            if (webs.getValue()) {
                slot = InventoryUtils.find(Items.COBWEB, 0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap  || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup  ? 35 : 8);
            } else {
                slot = InventoryUtils.findHardestBlock(0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap  || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup  ? 35 : 8);
            }
            int previousSlot = mc.player.getInventory().selectedSlot;

            if (slot == -1) {
                positions = new ArrayList<>();
                return;
            }

            if (mode.getValue() == Mode.Smart) {
                Target target = getTarget(players);
                if (target == null) positions = new ArrayList<>();
                else positions = target.positions();
            } else {
                positions = getPositions(null);
            }

            if (positions.isEmpty()) {
                return;
            }

            InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);

            for (BlockPos position : this.positions) {
                if (blocksPlaced >= bpt.getValue()) break;

                Direction direction = WorldUtils.getDirection(position, strictDirection.getValue());
                if (direction == null) continue;

                WorldUtils.placeBlock(position, direction, Hand.MAIN_HAND, rotate.getValue(), crystalDestruction.getValue(), render.getValue());
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
            if (holeCheck.getValue() && HoleUtils.isPlayerInHole(player)) continue;

            List<BlockPos> positions = getPositions(player);
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

    private List<BlockPos> getPositions(PlayerEntity player) {
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < DrugHack.getInstance().getWorldManager().getRadius(range.getValue().doubleValue()); i++) {
            BlockPos position = mc.player.getBlockPos().add(DrugHack.getInstance().getWorldManager().getOffset(i));
            Vec3d vec3d = Vec3d.ofCenter(position);

            if (!mc.world.getBlockState(position).isReplaceable()) continue;
            if (mc.player.squaredDistanceTo(vec3d) > MathHelper.square(range.getValue().doubleValue())) continue;
            if (mode.getValue() == Mode.Smart && player != null && player.squaredDistanceTo(vec3d) > MathHelper.square(smartRange.getValue().doubleValue())) continue;
            if (safety.getValue() && !HoleUtils.isPlayerInHole(mc.player) && mc.player.squaredDistanceTo(vec3d) <= MathHelper.square(safetyRange.getValue().doubleValue())) continue;
            if (HoleUtils.getSingleHole(position, 1.0) == null && (!doubleHoles.getValue() || HoleUtils.getDoubleHole(position, 1.0) == null)) continue;
            if (webs.getValue() && selfWeb.getValue() && PositionUtils.getFlooredPosition(mc.player).equals(position) && HoleUtils.isPlayerInHole(mc.player)
                    && player.getY() > mc.player.getY() && mc.player.distanceTo(player) <= 2 && mc.player.isOnGround()) {
                positions.add(position);
                continue;
            }
            if (!WorldUtils.isPlaceable(position)) continue;

            positions.add(position);
        }

        return positions;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        positions = new ArrayList<>();
    }

    private record Target(PlayerEntity player, List<BlockPos> positions) { }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Normal("Normal"),
        Smart("Smart");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}