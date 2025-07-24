package ru.drughack.modules.impl.player;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.*;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import lombok.*;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.rotations.RotationChanger;
import ru.drughack.utils.rotations.RotationUtils;
import ru.drughack.utils.world.HoleUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.WorldUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SpeedMine extends Module {

    public Setting<InventoryUtils.Switch> switchMode = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<Float> range = new Setting<>("Range", 6.0f, 0.0f, 6f);
    public Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.7f, 1.0f);
    public Setting<RotateMode> rotate = new Setting<>("Rotate", RotateMode.Packet);
    public Setting<Boolean> switchReset = new Setting<>("SwitchReset", true, v -> switchMode.getValue() == InventoryUtils.Switch.None ||  switchMode.getValue() == InventoryUtils.Switch.AltPickup ||  switchMode.getValue() == InventoryUtils.Switch.AltSwap);
    public Setting<Boolean> doubleMine = new Setting<>("Double", false);
    public Setting<CategoryBooleanSetting> instant = new Setting<>("Instant", new CategoryBooleanSetting(false));
    public Setting<Float> instantDelay = new Setting<>("Delay", 0f, 0f, 1000f, v -> instant.getValue().isOpen());
    public Setting<Boolean> grim = new Setting<>("Grim", false);
    public Setting<Boolean> strict = new Setting<>("Strict", false);
    public Setting<Boolean> whileEating = new Setting<>("WhileEating", true);
    public Setting<CategorySetting> renderCategory = new Setting<>("Render", new CategorySetting());
    public Setting<RenderMode> render = new Setting<>("Mode", RenderMode.Both, v -> renderCategory.getValue().isOpen());
    public Setting<AnimationMode> animation = new Setting<>("Animation", AnimationMode.In,  v -> renderCategory.getValue().isOpen() && (render.getValue() == RenderMode.Both || render.getValue() == RenderMode.Fill || render.getValue() == RenderMode.Outline));
    public Setting<ColorMode> color = new  Setting<>("Color", ColorMode.Progress, v -> renderCategory.getValue().isOpen() && (render.getValue() == RenderMode.Both || render.getValue() == RenderMode.Outline || render.getValue() == RenderMode.Fill));

    @Getter private Action primary = null;
    @Getter private Action secondary = null;
    private SwitchAction switchAction = null;
    private final TimerUtils instantTimer = new TimerUtils();
    private final TimerUtils mineTimer = new TimerUtils();
    private Vector2f rotations = new Vector2f(0, 0);
    private final RotationChanger changer = new RotationChanger(500, () -> new Float[]{rotations.getX(), rotations.getY()});

    public SpeedMine() {
        super("SpeedMine", "mine the blocks fastest", Category.Player);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (mc.player == null || mc.world == null) return;
        if (doubleMine.getValue() && secondary != null && secondary.process()) secondary = null;
        if (primary != null && primary.process()) primary = null;
        if (!DrugHack.getInstance().getModuleManager().getAutoMine().isToggled()) return;
        if ((primary != null && primary.getPriority() > 0 && !WorldUtils.isReplaceable(primary.getPosition())) || (secondary != null && secondary.getPriority() > 0 && !WorldUtils.isReplaceable(secondary.getPosition()))) return;

        Target target = getTarget();
        if (doubleMine.getValue()) {
            if (!mineTimer.hasTimeElapsed(350L)) return;

            if (mc.player.isCrawling() && DrugHack.getInstance().getModuleManager().getAutoMine().antiCrawl.getValue()) {
                BlockPos position;
                BlockPos playerPosition = mc.player.getBlockPos();
                if (WorldUtils.canBreak(playerPosition.down()) && !WorldUtils.isReplaceable(playerPosition.down()) && (!WorldUtils.isReplaceable(playerPosition.down(2)) || HoleUtils.getSingleHole(playerPosition.down(2), 1, false) != null)) position = playerPosition.down();
                else position = playerPosition.up();
                if (isValid(position) && !isOutOfRange(position)) {
                    if (!isInvalid(position)) handle(position, 0);
                    return;
                }
            }

            if ((primary != null && primary.isInstantMine() && primary.getAttempts() != 0) || secondary != null) return;

            if (target != null) {
                Runnable inside = () -> {
                    List<BlockPos> insidePositions = HoleUtils.getInsidePositions(target.player()).stream().filter(insidePosition -> !mc.world.getBlockState(insidePosition).isReplaceable()).toList();
                    for (BlockPos position : insidePositions) {
                        if (primary != null && secondary != null) break;
                        if (isInvalid(position) || isOutOfRange(position)) continue;
                        handle(position, 0);
                    }
                };
                Runnable outside = () -> {
                    List<BlockPos> surroundPositions = HoleUtils.getFeetPositions(target.player(), true, false, true).stream().filter(pos -> !mc.world.getBlockState(pos).isReplaceable()).toList();
                    if (HoleUtils.isPlayerInHole(target.player()) || !DrugHack.getInstance().getModuleManager().getAutoMine().onlyHole.getValue()) {
                        for (BlockPos position : surroundPositions) {
                            if (primary != null && secondary != null) break;
                            if (isMining(position)) continue;
                            if (isInvalid(position) || isOutOfRange(position)) continue;
                            handle(position, 0);
                        }
                    }
                };

                inside.run();
                outside.run();
            }
        } else {
            BlockPos position = null;

            if (target == null) return;
            else {
                if (!WorldUtils.isReplaceable(target.player.getBlockPos()) && !WorldUtils.getBlock(target.player().getBlockPos()).equals(Blocks.COBWEB)) position = target.player().getBlockPos();
                else if (HoleUtils.isPlayerInHole(target.player()) || !DrugHack.getInstance().getModuleManager().getAutoMine().onlyHole.getValue()) position = target.position();
            }

            if (position == null) return;
            if (primary != null && position.equals(primary.getPosition())) return;

            handle(position, 0);
        }
    }

    @EventHandler(priority = Integer.MAX_VALUE)
    public void onTick(EventTick e) {
        if (switchAction == null) return;
        if (System.currentTimeMillis() - switchAction.time() < 100L) return;
        if (mc.player != null && mc.world != null && (switchAction.slot() != -1 && switchAction.previousSlot() != -1)) InventoryUtils.switchBack(switchMode.getValue().name(), switchAction.slot(), switchAction.previousSlot());
        switchAction = null;
    }

    @EventHandler
    public void onRenderWorld(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        if (doubleMine.getValue() && secondary != null) secondary.render(event.getMatrices());
        if (primary != null) primary.render(event.getMatrices());
    }

    @EventHandler
    public void onPacketSend(EventPacketSend.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchReset.getValue() && (switchMode.getValue() == InventoryUtils.Switch.AltSwap || switchMode.getValue() == InventoryUtils.Switch.AltPickup)) {

            if (secondary != null) {
                secondary.cancel();
                secondary.start();
            }

            if (primary != null) {
                primary.cancel();
                primary.start();
            }
        }
    }

    @EventHandler
    public void onAttackBlock(EventAttackBlock event) {
        if (mc.player == null || mc.world == null) return;
        if (handle(event.getPosition(), 1)) event.cancel();
    }

    @Getter
    public class Action {
        private final BlockPos position;
        private BlockState state;
        private final int priority;
        @Setter private float progress;
        private float prevProgress;
        private int attempts;
        private boolean mining;
        private boolean instantMine;

        public Action(BlockPos position, int priority) {
            this.position = position;
            this.state = mc.world.getBlockState(position);
            this.priority = priority;
            start();
        }

        public boolean process() {
            if (isOutOfRange(position)) {
                cancel();
                return true;
            }

            boolean secondary = getSecondary() != null && position.equals(getSecondary().getPosition());
            if (secondary) instantMine = false;

            if (secondary && mc.world.getBlockState(position).isReplaceable()) {
                cancel();
                return true;
            }

            Direction direction = WorldUtils.getClosestDirection(position, true);
            BlockState state = mc.world.getBlockState(position);

            if (!state.isReplaceable() && state.getBlock() != this.state.getBlock()) {
                this.state = state;
            }

            if (mining) {
                int slot = switchMode.getValue() == InventoryUtils.Switch.None ? -1 : InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, switchMode.getValue() == InventoryUtils.Switch.AltSwap || switchMode.getValue() == InventoryUtils.Switch.AltPickup ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END);
                if (slot == -1) slot = mc.player.getInventory().selectedSlot;

                float delta = WorldUtils.getMineSpeed(this.state, slot) / DrugHack.getInstance().getWorldManager().getTimerMultiplier();

                prevProgress = progress;
                progress = MathHelper.clamp(progress + delta, 0.0f, getSpeed());

                rotations = new Vector2f(RotationUtils.getRotations(WorldUtils.getHitVector(position, direction))[0], RotationUtils.getRotations(WorldUtils.getHitVector(position, direction))[1]);

                if (rotate.getValue() == RotateMode.Normal && progress + (delta * 2) >= getSpeed()) DrugHack.getInstance().getRotationManager().addRotation(changer);

                if (progress >= getSpeed() && !state.isReplaceable() && (whileEating.getValue() || !mc.player.isUsingItem())) {
                    if (!instantMine || instantTimer.hasTimeElapsed(instantDelay.getValue().longValue())) {
                        DrugHack.getInstance().getEventHandler().post(new EventDestroyBlock(position));

                        if (rotate.getValue() == RotateMode.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(WorldUtils.getHitVector(position, direction)));

                        int previousSlot = mc.player.getInventory().selectedSlot;
                        InventoryUtils.switchSlot(switchMode.getValue().name(), slot, previousSlot);

                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, position, direction));
                        if (grim.getValue()) mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, position.up(500), direction));
                        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

                        if (strict.getValue() || (doubleMine.getValue() && secondary)) switchAction = new SwitchAction(slot, previousSlot, System.currentTimeMillis());
                        else if (switchAction == null) InventoryUtils.switchBack(switchMode.getValue().name(), slot, previousSlot);

                        if (!instantMine || secondary) mineTimer.reset();
                    }

                    attempts++;
                    if (!secondary) {
                        if (!instant.getValue().isEnabled()) {
                            start();
                        } else {
                            this.instantMine = true;
                            instantTimer.reset();
                        }
                    }

                    return doubleMine.getValue() && secondary;
                }
            } else {
                start();
            }

            return false;
        }

        public void render(MatrixStack matrices) {
            if (mc.world.getBlockState(position).isReplaceable()) return;

            Box box = new Box(position);
            double progress = MathHelper.lerp(mc.getRenderTickCounter().getTickDelta(false), prevProgress / getSpeed(), this.progress / getSpeed());
            if (animation.getValue() == AnimationMode.Out) {
                double scale = MathHelper.clamp(progress, 0.0, 1.0);
                double offset = (1.0 - scale) / 2.0;

                box = new Box(
                        position.getX() + offset,
                        position.getY() + offset,
                        position.getZ() + offset,
                        position.getX() + 1.0 - offset,
                        position.getY() + 1.0 - offset,
                        position.getZ() + 1.0 - offset
                );
            } else if (animation.getValue() == AnimationMode.In) {
                double scale = MathHelper.clamp(1.0 - progress, 0.0, 1.0);
                double offset = (1.0 - scale) / 2.0;

                box = new Box(position.getX() + offset,
                        position.getY() + offset,
                        position.getZ() + offset,
                        position.getX() + 1.0 - offset,
                        position.getY() + 1.0 - offset,
                        position.getZ() + 1.0 - offset
                );
            }

            Color fill = ColorUtils.getGlobalColor(50);
            Color outline = ColorUtils.getGlobalColor();

            if (color.getValue() == ColorMode.Custom) {
                fill = ColorUtils.getGlobalColor(50);
                outline = ColorUtils.getGlobalColor();
            } else if (color.getValue() == ColorMode.Progress) {
                fill = new Color(255 - (int) (MathHelper.clamp(progress, 0.0f, 1.0f) * 255), (int) (MathHelper.clamp(progress, 0.0f, 1.0f) * 255), 0, 50);
                outline = new Color(255 - (int) (MathHelper.clamp(progress, 0.0f, 1.0f) * 255), (int) (MathHelper.clamp(progress, 0.0f, 1.0f) * 255), 0, 255);
            }

            if (render.getValue() == RenderMode.Fill || render.getValue() == RenderMode.Both) Renderer3D.renderBox(matrices, box, fill);
            if (render.getValue() == RenderMode.Outline || render.getValue() == RenderMode.Both) Renderer3D.renderBoxOutline(matrices, box, outline);
        }

        public void start() {
            Direction direction = WorldUtils.getClosestDirection(position, true);
            if (doubleMine.getValue()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, position, direction));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, position, direction));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, position, direction));
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, position, direction));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, position, direction));
            }

            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.attempts = 0;
            this.mining = true;

            this.instantMine = false;
        }

        public void cancel() {
            if (!doubleMine.getValue()) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, position, WorldUtils.getClosestDirection(position, true)));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }

            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.attempts = 0;
            this.mining = false;

            this.instantMine = false;
        }

        public float getSpeed() {
            return getSecondary() != null && position.equals(getSecondary().getPosition()) ? 1.0f : speed.getValue().floatValue();
        }
    }

    private record SwitchAction(int slot, int previousSlot, long time) { }

    public boolean isOutOfRange(BlockPos position) {
        if (position == null) return true;
        return mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(range.getValue().doubleValue());
    }

    private record Target(PlayerEntity player, java.util.List<Position> feetPositions, BlockPos position) { }
    private record Position(BlockPos position, boolean feetPosition) { }

    private boolean handle(BlockPos position, int priority) {
        if (mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE || mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) return false;
        if (mc.world.getBlockState(position).getBlock().getHardness() == -1) return false;
        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(range.getValue().doubleValue())) return false;

        if ((primary != null && primary.getPosition().equals(position)) || (secondary != null && secondary.getPosition().equals(position))) return true;

        if (doubleMine.getValue()) {
            if (secondary != null) {
                primary = new Action(position, priority);
            } else {
                if (primary != null) {
                    if (!primary.isInstantMine()) secondary = primary;
                    primary = new Action(position, priority);
                } else {
                    primary = new Action(position, priority);
                }
            }
        } else {
            if (primary != null) primary.cancel();
            primary = new Action(position, priority);
        }

        return true;
    }

    private boolean isInvalid(BlockPos position) {
        if (!isValid(position)) return true;
        return isMining(position);
    }

    private boolean isValid(BlockPos position) {
        if (position == null) return false;
        if (mc.world.getBlockState(position).getBlock().getHardness() == -1) return false;
        return !mc.world.getBlockState(position).getBlock().equals(Blocks.COBWEB);
    }

    private boolean isMining(BlockPos position) {
        if (position == null) return true;
        if (primary != null && primary.getPosition().equals(position)) return true;
        return secondary != null && secondary.getPosition().equals(position);
    }

    private Target getTarget() {
        Target optimalTarget = null;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (!player.isAlive() || player.getHealth() <= 0.0f) continue;
            if (mc.player.squaredDistanceTo(player) > MathHelper.square(range.getValue().doubleValue() + 2.0)) continue;
            if (DrugHack.getInstance().getFriendManager().contains(player.getName().getString())) continue;

            List<Position> feetPositions = getPositions(player);
            BlockPos position = getTargetPosition(feetPositions);

            if (!doubleMine.getValue()) {
                if (feetPositions.isEmpty()) continue;
                if (position == null) continue;
            }

            if (optimalTarget == null) {
                optimalTarget = new Target(player, feetPositions, position);
                continue;
            }

            if (mc.player.squaredDistanceTo(player) < mc.player.squaredDistanceTo(optimalTarget.player())) {
                optimalTarget = new Target(player, feetPositions, position);
            }
        }

        return optimalTarget;
    }

    private BlockPos getTargetPosition(List<Position> positions) {
        BlockPos optimalPosition = null;
        double optimalScore = 0.0;

        for (Position position : positions) {
            if ((doubleMine.getValue() || DrugHack.getInstance().getModuleManager().getAutoMine().onlyCity.getValue()) && !position.feetPosition()) continue;
            if (!isValidPosition(position.position())) continue;
            if (HoleUtils.isPlayerInHole(mc.player) && HoleUtils.getFeetPositions(mc.player, true, false, true).contains(position.position())) continue;

            double score = 0.0;

            if (position.feetPosition()) {
                score += 0.05;

                if (mc.world.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST) score += 0.95;
                else if (WorldUtils.isCrystalPlaceable(position.position().add(0, 1, 0))) score += 0.35;
                if (hasCityPosition(position.position())) score += 0.6;
            } else {
                if (mc.world.getBlockState(position.position()).getBlock() == Blocks.ENDER_CHEST) {
                    score -= 2.0;
                } else {
                    if (WorldUtils.isCrystalPlaceable(position.position().add(0, 1, 0))) score += 0.75;
                    else score -= 2.0;
                }
            }

            if (score >= optimalScore) {
                optimalPosition = position.position();
                optimalScore = score;
            }
        }

        return optimalPosition;
    }

    private List<Position> getPositions(PlayerEntity player) {
        List<Position> positions = new ArrayList<>();

        for (BlockPos position : HoleUtils.getFeetPositions(player, true, false, true)) {
            positions.add(new Position(position, true));
            if (!doubleMine.getValue()) positions.add(new Position(position.add(0, 1, 0), false));
        }

        if (!doubleMine.getValue()) positions.add(new Position(player.getBlockPos().add(0, 2, 0), false));
        return positions;
    }

    private boolean hasCityPosition(BlockPos position) {
        Vec3i[] offsets = new Vec3i[]{new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1)};

        for (Vec3i vec3i : offsets) {
            BlockPos offsetPosition = position.add(vec3i);
            if (WorldUtils.isPlaceable(offsetPosition)) return true;
        }

        return false;
    }

    private boolean isValidPosition(BlockPos position) {
        if (mc.world.getBlockState(position).isReplaceable()) return false;
        if (mc.world.getBlockState(position).getBlock().getHardness() == -1) return false;
        return !isOutOfRange(position);
    }

    @Override
    public String getDisplayInfo() {
        String primaryProgress = primary == null ? "0.0" : new DecimalFormat("0.0").format(primary.getProgress() / primary.getSpeed());
        String secondaryProgress = secondary == null || !doubleMine.getValue() ? "" : ", " + new DecimalFormat("0.0").format(secondary.getProgress() / secondary.getSpeed());
        return primaryProgress + secondaryProgress;
    }

    @AllArgsConstructor
    public enum RotateMode implements Nameable {
        None("None"),
        Normal("Normal"),
        Packet("Packet");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum RenderMode implements Nameable {
        None("None"),
        Fill("Fill"),
        Outline("Outline"),
        Both("Both");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum ColorMode implements Nameable {
        Progress("Progress"),
        Custom("Custom");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum AnimationMode implements Nameable {
        Plain("Plain"),
        In("In"),
        Out("Out");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}