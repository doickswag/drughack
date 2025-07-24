package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.event.impl.EventJumpPlayer;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.impl.exploit.HitboxDesync;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.utils.world.HoleUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.PositionUtils;
import ru.drughack.utils.world.WorldUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeetTrap extends Module {

    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<Timing> timing = new Setting<>("Timing", Timing.Sequential);
    public Setting<Integer> bpt = new Setting<>("BPT", 4, 1, 20);
    public Setting<Integer> delay = new Setting<>("Delay", 0, 0, 1000);
    public Setting<Integer> range = new Setting<>("Range", 5, 0, 12);
    public Setting<Boolean> await = new Setting<>("Await", false);
    public Setting<Boolean> rotate = new Setting<>("Rotate",  true);
    public Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    public Setting<Boolean> crystalDestruction = new Setting<>("CrystalDestruction", true);
    public Setting<Boolean> center = new Setting<>("Center", false);
    public Setting<Boolean> floor = new Setting<>("Floor", true);
    public Setting<Boolean> extension = new Setting<>("Extension", true);
    public Setting<Boolean> whileEating = new Setting<>("WhileEating", true);
    public Setting<CategorySetting> autoDisable = new Setting<>("AutoDisable", new CategorySetting());
    public Setting<Boolean> jumpDisable = new Setting<>("Jump", true, v -> autoDisable.getValue().isOpen());
    public Setting<Boolean> render = new Setting<>("Render",  true);

    public FeetTrap() {
        super("FeetTrap", "Trap your femboys feet", Category.Combat);
    }

    private Set<BlockPos> targetPositions = new HashSet<>();
    private BlockPos lastPosition = null;

    private long lastTime = 0;
    private int blocksPlaced = 0;

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null || mc.world == null) return;
        lastPosition = PositionUtils.getFlooredPosition(mc.player);
        lastTime = System.currentTimeMillis();

        if (center.getValue()) mc.player.setPosition(lastPosition.getX() + 0.5, lastPosition.getY(), lastPosition.getZ() + 0.5);
    }

    @EventHandler
    public void onPlayerJump(EventJumpPlayer event) {
        if (jumpDisable.getValue()) {
            toggle();
        }
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (jumpDisable.getValue() && (DrugHack.getInstance().getServerManager().getFallDistance() > 2.0f || ((DrugHack.getInstance().getModuleManager().getStep().isToggled() || DrugHack.getInstance().getModuleManager().getSpeed().isToggled()) && (lastPosition == null || lastPosition.getY() != PositionUtils.getFlooredPosition(mc.player).getY())))) {
            toggle();
            return;
        }

        if (!whileEating.getValue() && mc.player.isUsingItem()) return;
        blocksPlaced = 0;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime < delay.getValue()) return;

        if (autoSwitch.getValue() == InventoryUtils.Switch.None && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
            targetPositions.clear();
            return;
        }

        int slot = InventoryUtils.findHardestBlock(0, 8);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (slot == -1) {
            targetPositions.clear();
            return;
        }

        targetPositions = HoleUtils.getFeetPositions(mc.player, extension.getValue(), floor.getValue(), false);

        HitboxDesync module = DrugHack.getInstance().getModuleManager().getHitboxDesync();
        List<BlockPos> positions = targetPositions.stream().filter(position -> mc.player.squaredDistanceTo(Vec3d.ofCenter(position)) <= MathHelper.square(range.getValue().doubleValue()))
                .filter(position -> WorldUtils.isPlaceable(position, module.isToggled() && !module.close.getValue()))
                .toList();

        if (positions.isEmpty()) {
            return;
        }

        InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);

        List<BlockPos> placedPositions = new ArrayList<>();
        for (BlockPos position : positions) {
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

        lastTime = currentTime;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.world == null) return;
        if (!(timing.getValue() == Timing.Sequential)) return;

        if (event.getPacket() instanceof EntitySpawnS2CPacket packet && packet.getEntityType().equals(EntityType.END_CRYSTAL)) {
            EndCrystalEntity crystal = new EndCrystalEntity(mc.world, packet.getX(), packet.getY(), packet.getZ());
            for (BlockPos position : targetPositions) {
                if (new Box(position).intersects(crystal.getBoundingBox()) && targetPositions.contains(position)) {
                    if (blocksPlaced > bpt.getValue()) return;
                    if (!whileEating.getValue() && mc.player.isUsingItem()) return;
                    int slot = InventoryUtils.findHardestBlock(0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
                    int previousSlot = mc.player.getInventory().selectedSlot;
                    if (slot == -1) return;
                    Direction direction = WorldUtils.getDirection(position, strictDirection.getValue());
                    if (direction == null) return;
                    InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);
                    WorldUtils.placeBlock(position, direction, Hand.MAIN_HAND, () -> {
                        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
                        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }, rotate.getValue(), false, render.getValue());
                    blocksPlaced++;
                    InventoryUtils.switchBack(autoSwitch.getValue().name(), slot, previousSlot);
                    break;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        lastPosition = null;
        targetPositions.clear();
        lastTime = 0;
        blocksPlaced = 0;
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(targetPositions.size());
    }

    @AllArgsConstructor
    private enum Timing implements Nameable {
        Vanilla("Vanilla"),
        Sequential("Sequential");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}