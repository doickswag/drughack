package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.*;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.rotations.RotationChanger;
import ru.drughack.utils.rotations.RotationUtils;
import ru.drughack.utils.world.*;

public class Aura extends Module {

    public Setting<AutoSwitch> autoSwitch = new Setting<>("Switch", AutoSwitch.None);
    public Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.Normal);
    public Setting<CategoryBooleanSetting> sprintReset = new Setting<>("Sprint Reset", new CategoryBooleanSetting(false));
    public Setting<Integer> ticksReset = new Setting<>("Ticks", 1, 1, 10, v -> sprintReset.getValue().isOpen());
    public Setting<CategoryBooleanSetting> forward = new Setting<>("Forward", new CategoryBooleanSetting(false));
    public Setting<Integer> forwardValue = new Setting<>("Value", 3, 1, 6, v -> forward.getValue().isOpen());
    public Setting<InventoryUtils.Swing> swing = new Setting<>("Swing", InventoryUtils.Swing.Mainhand);
    public Setting<Float> range = new Setting<>("Range", 5.2f, 0.0f, 6f);
    public Setting<Float> wallsRange = new Setting<>("WallsRange", 5.2f, 0f, 6f);
    public Setting<Float> aimRange = new Setting<>("AimRange", 3f, 0f, 6f);
    public Setting<Float> elytraAimRange = new Setting<>("ElytraAimRange", 50f, 10f, 100f);
    public Setting<CategorySetting> targets = new Setting<>("Targets", new CategorySetting());
    public Setting<Boolean> players = new Setting<>("Players", true, v -> targets.getValue().isOpen());
    public Setting<Boolean> friends = new Setting<>("Friends", false, v -> targets.getValue().isOpen());
    public Setting<Boolean> animals = new Setting<>("Animals", false, v -> targets.getValue().isOpen());
    public Setting<Boolean> hostiles = new Setting<>("Hostiles",false, v -> targets.getValue().isOpen());
    public Setting<Boolean> passives = new Setting<>("Passives", false, v -> targets.getValue().isOpen());
    public Setting<Boolean> ambient = new Setting<>("Ambient", false, v -> targets.getValue().isOpen());
    public Setting<Boolean> invisibles = new Setting<>("Invisibles", false, v -> targets.getValue().isOpen());
    public Setting<Boolean> boats = new Setting<>("Boats", false, v -> targets.getValue().isOpen());
    public Setting<Boolean> shulkerBullets = new Setting<>("ShulkerBullets", true, v -> targets.getValue().isOpen());
    public Setting<Render> render = new Setting<>("Render", Render.Both);

    @Getter private Entity target, aimTarget;
    private Vector2f rotations = new Vector2f(0, 0);
    private final RotationChanger changer = new RotationChanger(500, () -> new Float[]{rotations.getX(), rotations.getY()});
    private boolean attacking, shouldAttack;
    private int ticks;

    public Aura() {
        super("Aura", "Hits your enemy with your femboys hands", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (fullNullCheck()) return;

        Iterable<Entity> allEntities = mc.world.getOtherEntities(
                mc.player,
                PositionUtils.getRadius(mc.player, getAimRange() + 1.0),
                entity -> {
                    boolean isAlive = entity.isAlive();
                    boolean hasHealth = !(entity instanceof LivingEntity livingEntity) || livingEntity.getHealth() > 0.0f;
                    Vec3d multipoint = MultipointUtils.getClosestPoint(entity, 10, 10, getAimRange(), false);
                    if (multipoint == null) return false;
                    boolean inRange = multipoint.squaredDistanceTo(mc.player.getEyePos()) < MathHelper.square(getAimRange());
                    boolean insideWorldBorder = mc.world.getWorldBorder().contains(entity.getBlockPos());
                    boolean isNotFriend = friends.getValue() || !DrugHack.getInstance().getFriendManager().contains(entity.getName().getString());
                    boolean validEntity = isValidEntity(entity);
                    boolean canTarget = WorldUtils.canSee(entity) || multipoint.squaredDistanceTo(mc.player.getEyePos()) < MathHelper.square(wallsRange.getValue().doubleValue());
                    return isAlive && hasHealth && inRange && insideWorldBorder && isNotFriend && validEntity && canTarget;
                }
        );

        Entity optimalAttackEntity = null;
        Entity optimalAimEntity = null;

        for (Entity entity : allEntities) {
            Vec3d multipoint = MultipointUtils.getClosestPoint(entity, 10, 10, getAimRange(), false);
            Vec3d vec3d = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
            if (multipoint == null) continue;

            boolean inAttackRange = multipoint.squaredDistanceTo(vec3d) < MathHelper.square(getRange());

            if (inAttackRange) {
                if (optimalAttackEntity == null ||
                        multipoint.squaredDistanceTo(vec3d) < MultipointUtils.getClosestPoint(optimalAttackEntity, 10, 10, getRange(), false).squaredDistanceTo(vec3d)) {
                    optimalAttackEntity = entity;
                }
            }

            if (optimalAimEntity == null ||
                    multipoint.squaredDistanceTo(vec3d) < MultipointUtils.getClosestPoint(optimalAimEntity, 10, 10, getAimRange(), false).squaredDistanceTo(vec3d)) {
                optimalAimEntity = entity;
            }
        }

        target = optimalAttackEntity;
        aimTarget = optimalAimEntity;
        attacking = false;
        shouldAttack = false;

        if (aimTarget != null) {
            if (autoSwitch.getValue() == AutoSwitch.Require && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
            int slot = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (autoSwitch.getValue() == AutoSwitch.Normal && slot == -1) return;

            if (rotate.getValue() == Rotate.Normal) {
                Vec3d pos = new Vec3d(aimTarget.getX(), aimTarget.getEyeY() - 0.25f, aimTarget.getZ());

                if (mc.player.isGliding()) {
                    Vec3d position = aimTarget.getPos();
                    if (forward.getValue().isEnabled()) {
                        Vec3d scale = Vec3d.fromPolar(new Vec2f(aimTarget.getYaw(), aimTarget.getPitch()))
                                .normalize()
                                .multiply(forwardValue.getValue());
                        pos = position.add(scale);
                    }
                }

                Vec3d finalPos = new Vec3d(pos.x, pos.y, pos.z);
                rotations = new Vector2f(RotationUtils.getRotations(finalPos)[0], RotationUtils.getRotations(finalPos)[1]);
                DrugHack.getInstance().getRotationManager().addRotation(changer);
            }
        }

        if (target != null) {
            attacking = true;
            if (shouldCrit()) return;
            if (rotate.getValue() == Rotate.Snap) {
                rotations = new Vector2f(RotationUtils.getRotations(target)[0], RotationUtils.getRotations(target)[1]);
                if (!DrugHack.getInstance().getRotationManager().containsRotation(changer)) DrugHack.getInstance().getRotationManager().addRotation(changer);
                else DrugHack.getInstance().getRotationManager().removeRotation(changer);
            }
            shouldAttack = true;
        }
    }

    @EventHandler
    public void onUpdateMovement$POST(EventMoveUpdate.Post event) {
        if (mc.player == null || mc.world == null || !shouldAttack || !attacking || target == null || shouldCrit()) {
            shouldAttack = false;
            return;
        }

        if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(target));
        if (autoSwitch.getValue() == AutoSwitch.Normal) InventoryUtils.switchSlot("Normal", InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END), mc.player.getInventory().selectedSlot);
        if (autoSwitch.getValue() == AutoSwitch.Silent) InventoryUtils.switchSlot("Silent", InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END), mc.player.getInventory().selectedSlot);
        if (sprintReset.getValue().isEnabled() && mc.player.isSprinting() && DrugHack.getInstance().getServerManager().getFallDistance() > 0) ticks = ticksReset.getValue();
        mc.interactionManager.attackEntity(mc.player, target);
        switch (swing.getValue()) {
            case InventoryUtils.Swing.Mainhand -> mc.player.swingHand(Hand.MAIN_HAND);
            case InventoryUtils.Swing.Offhand -> mc.player.swingHand(Hand.OFF_HAND);
            case InventoryUtils.Swing.Both -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.swingHand(Hand.OFF_HAND);
            }
            case InventoryUtils.Swing.Packet -> mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        if (target instanceof PlayerEntity) shieldBreak((PlayerEntity) target);

        shouldAttack = false;
    }

    @EventHandler
    public void onRenderWorld(EventRender3D event) {
        if (aimTarget == null) return;
        if (render.getValue() == Render.None) return;
        Vec3d vec3d = EntityUtils.getRenderPos(aimTarget, event.getTickDelta());
        Box box = new Box(vec3d.x - aimTarget.getBoundingBox().getLengthX() / 2, vec3d.y, vec3d.z - aimTarget.getBoundingBox().getLengthZ() / 2, vec3d.x + aimTarget.getBoundingBox().getLengthX() / 2, vec3d.y + aimTarget.getBoundingBox().getLengthY(), vec3d.z + aimTarget.getBoundingBox().getLengthZ() / 2);
        if (render.getValue() == Render.Fill || render.getValue() == Render.Both) Renderer3D.renderBox(event.getMatrices(), box, ColorUtils.getGlobalColor(50));
        if (render.getValue() == Render.Outline || render.getValue() == Render.Both) Renderer3D.renderBoxOutline(event.getMatrices(), box, ColorUtils.getGlobalColor());
    }

    @EventHandler
    public void onKeyboardTick(EventKeyboardTick e) {
        if (sprintReset.getValue().isEnabled() && ticks > 0) {
            e.setMovementForward(0);
            ticks--;
            e.cancel();
        }
    }

    @EventHandler
    public void onFireworkVector(EventFireworkVector e) {
        if (fullNullCheck()) return;

        if (aimTarget != null
                && rotations.getX() != 0
                && rotations.getY() != 0
                && rotate.getValue() == Rotate.Normal
        ) {
            e.setVector(RotationUtils.getVecRotations(rotations.getX(), rotations.getY()));
            e.cancel();
        }
    }

    public double getRange() {
        if (mc.player.isGliding()) return 1.5f;
        return range.getValue();
    }

    public double getAimRange() {
        if (mc.player.isGliding()) return elytraAimRange.getValue();
        return range.getValue() + aimRange.getValue();
    }

    private void shieldBreak(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int axeSlot = InventoryUtils.findBestAxe(0, 8);
            int previousSlot = mc.player.getInventory().selectedSlot;
            if (axeSlot != -1) {
                InventoryUtils.switchSlot("Silent", axeSlot, previousSlot);
                mc.interactionManager.attackEntity(mc.player, entity);
                switch (swing.getValue()) {
                    case InventoryUtils.Swing.Mainhand -> mc.player.swingHand(Hand.MAIN_HAND);
                    case InventoryUtils.Swing.Offhand -> mc.player.swingHand(Hand.OFF_HAND);
                    case InventoryUtils.Swing.Both -> {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.OFF_HAND);
                    }
                    case InventoryUtils.Swing.Packet -> mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
                InventoryUtils.switchBack("Silent", axeSlot, previousSlot);
            }
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (players.getValue() && entity.getType() == EntityType.PLAYER) return true;
        if (hostiles.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.MONSTER) {
            if (!passives.getValue() && entity instanceof EndermanEntity enderman && !enderman.isAngry()) return false;
            return passives.getValue() || !(entity instanceof ZombifiedPiglinEntity piglin) || piglin.isAttacking();
        }

        if (animals.getValue() && (entity.getType().getSpawnGroup() == SpawnGroup.CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.WATER_CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.WATER_AMBIENT || entity.getType().getSpawnGroup() == SpawnGroup.UNDERGROUND_WATER_CREATURE || entity.getType().getSpawnGroup() == SpawnGroup.AXOLOTLS)) return true;
        if (ambient.getValue() && entity.getType().getSpawnGroup() == SpawnGroup.AMBIENT) return true;
        if (invisibles.getValue() && entity.isInvisible()) return true;
        if (boats.getValue() && entity instanceof BoatEntity) return true;
        return shulkerBullets.getValue() && entity.getType() == EntityType.SHULKER_BULLET;
    }

    private boolean shouldCrit() {
        boolean cancelReason = mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isHoldingOntoLadder()
                || mc.player.getAbilities().flying;

        boolean onSpace = !mc.options.jumpKey.isPressed() && mc.player.isOnGround();
        float attackStrength = mc.player.getAttackCooldownProgress(1.0f);
        if (attackStrength < 0.92) return true;
        if (!cancelReason) return !onSpace && (mc.player.isOnGround() || !(DrugHack.getInstance().getServerManager().getFallDistance() > 0));

        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DrugHack.getInstance().getRotationManager().removeRotation(changer);
    }

    @Override
    public String getDisplayInfo() {
        return target == null ? "None" : target.getName().getString();
    }

    @AllArgsConstructor
    private enum AutoSwitch implements Nameable {
        Normal("Normal"),
        Silent("Silent"),
        Require("Require"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Rotate implements Nameable {
        Snap("Snap"),
        Normal("Normal"),
        Packet("Packet"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    private enum Render implements Nameable {
        Fill("Fill"),
        Outline("Outline"),
        Both("Both"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}