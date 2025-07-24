package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.*;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.impl.player.SpeedMine;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.utils.math.*;
import ru.drughack.utils.rotations.RotationChanger;
import ru.drughack.utils.rotations.RotationUtils;
import ru.drughack.utils.world.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//SWAG
public class AutoCrystal extends Module {

    public Setting<CategoryBooleanSetting> categoryBreak = new Setting<>("Break", new CategoryBooleanSetting(true));
    public Setting<Float> breakDelay = new Setting<>("Delay", 0f, 0f, 1000f, v -> categoryBreak.getValue().isOpen());
    public Setting<Float> breakRange = new Setting<>("Range", 6f, 0.0f, 6f, v -> categoryBreak.getValue().isOpen());
    public Setting<Float> breakWallsRange = new Setting<>("WallsRange",  6f, 0.0f, 6f, v -> categoryBreak.getValue().isOpen());
    public Setting<Weakness> weakness = new Setting<>("Weakness", Weakness.None, v -> categoryBreak.getValue().isOpen());
    public Setting<Sequential> sequential = new Setting<>("Sequential",  Sequential.Strong, v -> categoryBreak.getValue().isOpen());
    public Setting<Boolean> instant = new Setting<>("Instant", true, v -> categoryBreak.getValue().isOpen());
    public Setting<Boolean> inhibit = new Setting<>("Inhibit", true, v -> categoryBreak.getValue().isOpen());

    public Setting<CategoryBooleanSetting> categoryPlace = new Setting<>("Place", new CategoryBooleanSetting(true));
    public Setting<Float> placeDelay = new Setting<>("Delay", 0f, 0f, 1000f, v -> categoryPlace.getValue().isOpen());
    public Setting<Float> placeRange = new Setting<>("Range", 6f, 0.0f, 6f, v -> categoryPlace.getValue().isOpen());
    public Setting<Float> placeWallsRange = new Setting<>("WallsRange", 6f, 0.0f, 6f, v -> categoryPlace.getValue().isOpen());
    public Setting<Placements> placements = new Setting<>("Placements", Placements.Protocol, v -> categoryPlace.getValue().isOpen());
    public Setting<Boolean> blockDestruction = new Setting<>("BlockDestruction",  true, v -> categoryPlace.getValue().isOpen());

    public Setting<CategoryBooleanSetting> categoryFacePlace = new Setting<>("FacePlace", new CategoryBooleanSetting(true));
    public Setting<Float> healthAmount = new Setting<>("Health", 8.0f, 0.0f, 36.0f, v -> categoryFacePlace.getValue().isOpen());
    public Setting<Integer> armorPercentage = new Setting<>("Armor", 10, 1, 100, v -> categoryFacePlace.getValue().isOpen());
    public Setting<Boolean> damageSync = new Setting<>("DamageSync", false, v -> categoryFacePlace.getValue().isOpen());

    public Setting<CategoryBooleanSetting> categoryRotate = new Setting<>("Rotate", new CategoryBooleanSetting(true));
    public Setting<Rotate> rotate = new Setting<>("Mode", Rotate.Normal, v -> categoryRotate.getValue().isOpen());
    public Setting<Boolean> yawStep = new Setting<>("YawStep", false, v -> categoryRotate.getValue().isOpen());
    public Setting<Integer> yawStepThreshold = new Setting<>("YawStepThreshold", 75, 1, 180, v -> yawStep.getValue() && categoryRotate.getValue().isOpen());
    
    public Setting<CategorySetting> categoryRender = new Setting<>("Render", new CategorySetting());
    public Setting<RenderMode> renderMode = new Setting<>("Mode", RenderMode.Both, v -> categoryRender.getValue().isOpen());
    public Setting<Boolean> renderDamage = new Setting<>("Damage", false, v -> categoryRender.getValue().isOpen());

    public Setting<CategorySetting> categoryDamage = new Setting<>("Damage", new CategorySetting());
    public Setting<Float> minDamage = new Setting<>("MinDamage", 6.0f, 0.0f, 36.0f, v -> categoryDamage.getValue().isOpen());
    public Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 10.0f, 0.0f, 36.0f, v -> categoryDamage.getValue().isOpen());
    public Setting<Float> lethalMultiplier = new Setting<>("LethalMultiplier", 2f, 0.0f, 4.0f, v -> categoryDamage.getValue().isOpen());
    public Setting<Boolean> antiSuicide = new Setting<>("AntiSuicide", true, v -> categoryDamage.getValue().isOpen());
    public Setting<Boolean> ignoreTerrain = new Setting<>("IgnoreTerrain", true, v -> categoryDamage.getValue().isOpen());

    public Setting<CategorySetting> categoryMisc = new Setting<>("Misc", new CategorySetting());
    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("AutoSwitch", InventoryUtils.Switch.Silent, v -> categoryMisc.getValue().isOpen());
    public Setting<Swing> swing = new Setting<>("Swing", Swing.Default, v -> categoryMisc.getValue().isOpen());
    public Setting<Boolean> raytrace = new Setting<>("Raytrace", false, v -> categoryMisc.getValue().isOpen());
    public Setting<Integer> extrapolation = new Setting<>("Extrapolation", 1, 0, 20, v -> categoryMisc.getValue().isOpen());
    public Setting<Float> enemyRange = new Setting<>("EnemyRange", 10.0f, 0.0f, 24.0f, v -> categoryMisc.getValue().isOpen());
    public Setting<WhileEating> whileEating = new Setting<>("WhileEating", WhileEating.Both, v -> categoryMisc.getValue().isOpen());

    private Runnable breakRunnable = null;
    private Runnable placeRunnable = null;

    private final Map<Integer, Long> brokenCrystals = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> placedCrystals = new ConcurrentHashMap<>();
    private final Map<BlockPos, Long> countedCrystals = new ConcurrentHashMap<>();

    private final TimerUtils breakTimer = new TimerUtils();
    private final TimerUtils placeTimer = new TimerUtils();
    private final TimerUtils facePlaceTimer = new TimerUtils();

    private boolean sequenceBreak = false;
    private boolean sequencePlace = true;

    private boolean brokenSequentially = false;
    private boolean placedSequentially = false;

    @Getter private PlayerEntity target = null;

    private EndCrystalEntity breakTarget = null;
    private PlaceTarget placeTarget = null;
    private PlaceTarget mineTarget = null;

    @Getter private String calculationDamage = "0.00";
    private final Counter crystalCounter = new Counter();
    private int crystalsPerSecond = 0;
    private int highestID = -100000;
    private long facePlaceSpeed = 0L;

    private Vector2f rotations = new Vector2f(0, 0);
    private final RotationChanger changer = new RotationChanger(500, () -> new Float[]{rotations.getX(), rotations.getY()});

    public AutoCrystal() {
        super("AutoCrystal", "crystals your targets", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (mc.player == null || mc.world == null) return;

        brokenCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > DrugHack.getInstance().getServerManager().getPing() * 2L);
        placedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > DrugHack.getInstance().getServerManager().getPing() * 2L + (breakDelay.getValue()));
        countedCrystals.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > DrugHack.getInstance().getServerManager().getPing() * 2L);

        crystalsPerSecond = crystalCounter.getCount();

        Runnable runnable = () -> {
            breakTarget = calculateCrystals();
            placeTarget = calculatePlacements(null);
            calculationDamage = placeTarget == null ? "0.00" : new DecimalFormat("0.00").format(placeTarget.getDamage());
            target = placeTarget == null ? null : placeTarget.getPlayer();
        };

        runnable.run();
        run();
    }
    


    private void run() {
        breakRunnable = null;
        placeRunnable = null;

        if (sequential.getValue() == Sequential.None) {
            if (sequenceBreak) {
                sequenceBreak = false;
                sequencePlace = true;

                breakCrystals();
                return;
            }

            if (sequencePlace) {
                sequenceBreak = true;
                sequencePlace = false;

                placeCrystals(false);
            }
        } else {
            if (categoryBreak.getValue().isEnabled()) breakCrystals();
            if (categoryPlace.getValue().isEnabled()) placeCrystals(false);
        }
    }

    @EventHandler
    public void onUpdateMovement$POST(EventMoveUpdate.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (breakRunnable != null) breakRunnable.run();
        if (placeRunnable != null) placeRunnable.run();
    }

    @EventHandler
    public void onEntitySpawn(EventEntitySpawn event) {
        if (mc.player == null || mc.world == null) return;
        if (!categoryBreak.getValue().isEnabled() || !instant.getValue()) return;
        if (!breakTimer.hasTimeElapsed(breakDelay.getValue())) return;
        if (!(event.getEntity() instanceof EndCrystalEntity crystal)) return;
        if (inhibit.getValue() && brokenCrystals.containsKey(crystal.getId())) return;
        if (!placedCrystals.containsKey(crystal.getBlockPos().down())) return;
        Vec3d multipoint = MultipointUtils.getClosestPoint(crystal, 5, 5, breakRange.getValue().doubleValue(), raytrace.getValue());
        if (multipoint == null) return;
        if (multipoint.squaredDistanceTo(mc.player.getEyePos()) >= MathHelper.square(breakRange.getValue().doubleValue())) return;
        if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) return;
        if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || multipoint.squaredDistanceTo(mc.player.getEyePos()) >= MathHelper.square(breakWallsRange.getValue().doubleValue()))) return;
        rotations = new Vector2f(calculateRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0))[0], calculateRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0))[1]);
        if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0)));
        if (rotate.getValue() == Rotate.Normal) DrugHack.getInstance().getRotationManager().addRotation(changer);
        Break(crystal);
        brokenSequentially = true;
        if (sequential.getValue() == Sequential.Strong) placeCrystals(true);
    }

    @EventHandler
    public void onDestroyBlock(EventDestroyBlock event) {
        if (mc.player == null || mc.world == null) return;
        if (!blockDestruction.getValue()) return;
        if (!placeTimer.hasTimeElapsed(placeDelay.getValue())) return;
        BlockPos minedPosition = event.getPosition();
        if (minedPosition == null) return;
        int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        if (!(autoSwitch.getValue() == InventoryUtils.Switch.None) && slot == -1 && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)) return;
        PlaceTarget mineTarget = this.mineTarget == null ? null : this.mineTarget.clone();
        if (mineTarget == null || (mineTarget.getPosition() != null && !minedPosition.equals(mineTarget.getException()))) mineTarget = calculatePlacements(minedPosition);

        if (mineTarget == null || mineTarget.getPosition() == null) {
            DrugHack.getInstance().getRenderManager().setRenderPosition(null);
            return;
        }

        BlockPos position = mineTarget.getPosition();
        DrugHack.getInstance().getRenderManager().setRenderPosition(position);

        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeRange.getValue().doubleValue())) return;
        if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeWallsRange.getValue().doubleValue()))) return;

        rotations = new Vector2f(calculateRotations(Vec3d.ofCenter(position, 1))[0], calculateRotations(Vec3d.ofCenter(position, 1))[1]);
        if (rotate.getValue() == Rotate.Normal && categoryRotate.getValue().isEnabled()) DrugHack.getInstance().getRotationManager().addRotation(changer);
        if (rotate.getValue() == Rotate.Packet && categoryRotate.getValue().isEnabled()) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(Vec3d.ofCenter(position, 1)));

        for (Entity entity : mc.world.getOtherEntities(null, new Box(position.up())).stream().filter(entity -> entity instanceof EndCrystalEntity).toList()) {
            if (rotate.getValue() == Rotate.Packet && categoryRotate.getValue().isEnabled()) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(entity));
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            break;
        }

        SpeedMine module = DrugHack.getInstance().getModuleManager().getSpeedMine();
        boolean flag = module.switchReset.getValue() && (module.switchMode.getValue() == InventoryUtils.Switch.Normal || module.switchMode.getValue() == InventoryUtils.Switch.AltSwap || module.switchMode.getValue() == InventoryUtils.Switch.AltPickup);

        if (!(autoSwitch.getValue() == InventoryUtils.Switch.None) &&  mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
            InventoryUtils.switchSlot(flag ? "AltSwap" : autoSwitch.getValue().name(), slot, previousSlot);
            switched = true;
        }

        Place(position);

        if (switched) InventoryUtils.switchBack(flag ? "AltSwap" : autoSwitch.getValue().name(), slot, previousSlot);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.world == null) return;

        if (event.getPacket() instanceof EntitySpawnS2CPacket packet) {
            if (packet.getEntityId() > highestID) highestID = packet.getEntityId();
            BlockPos position = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ()).add(0, -1, 0);

            if (countedCrystals.containsKey(position)) {
                if (damageSync.getValue()) facePlaceSpeed = 1500L;
                else facePlaceSpeed = 0L;
                if (facePlaceTimer.hasTimeElapsed(facePlaceSpeed)) facePlaceTimer.reset();
                countedCrystals.remove(position);
                crystalCounter.increment();
                crystalsPerSecond = crystalCounter.getCount();
            }
        }

        if (event.getPacket() instanceof ExperienceOrbSpawnS2CPacket packet) if (packet.getEntityId() > highestID) highestID = packet.getEntityId();
    }

    @EventHandler
    public void onClientConnect(EventClientConnect event) {
        highestID = -100000;
    }

    private void breakCrystals() {
        EndCrystalEntity overrideCrystal = null;

        boolean flag = placeTarget != null && placeTarget.obstructions != null && !placeTarget.obstructions.isEmpty();
        for (Entity entity : flag ? placeTarget.obstructions : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (!crystal.isAlive()) continue;
            if (inhibit.getValue() && brokenCrystals.containsKey(entity.getId())) continue;
            if (!flag && !placedCrystals.containsKey(crystal.getBlockPos().down())) continue;
            Vec3d multipoint = MultipointUtils.getClosestPoint(crystal, 5, 5, breakRange.getValue().doubleValue(), raytrace.getValue());
            if (multipoint == null) return;
            if (multipoint.squaredDistanceTo(mc.player.getEyePos()) >= MathHelper.square(breakRange.getValue().doubleValue())) continue;
            if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) continue;
            if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || multipoint.squaredDistanceTo(mc.player.getEyePos()) >= MathHelper.square(breakWallsRange.getValue().doubleValue()))) continue;

            overrideCrystal = crystal;
            break;
        }

        EndCrystalEntity crystal = overrideCrystal == null ? breakTarget : overrideCrystal;
        if (crystal == null) return;
        rotations = new Vector2f(calculateRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0))[0], calculateRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0))[1]);
        if (rotate.getValue() == Rotate.Normal) DrugHack.getInstance().getRotationManager().addRotation(changer);

        if (!breakTimer.hasTimeElapsed(breakDelay.getValue()) || brokenSequentially) {
            if (brokenSequentially) brokenSequentially = false;
            return;
        }

        Entity entity = mc.world.getEntityById(crystal.getId());
        if (entity == null) return;

        if (!(entity instanceof EndCrystalEntity endCrystal)) return;
        if (!endCrystal.isAlive()) return;
        if (inhibit.getValue() && brokenCrystals.containsKey(entity.getId())) return;
        Vec3d multipoint = MultipointUtils.getClosestPoint(endCrystal, 5, 5, breakRange.getValue().doubleValue(), raytrace.getValue());
        if (multipoint == null) return;
        if (multipoint.squaredDistanceTo(mc.player.getEyePos()) >= MathHelper.square(breakRange.getValue().doubleValue())) return;
        if (!mc.world.getWorldBorder().contains(endCrystal.getBlockPos())) return;
        if (!WorldUtils.canSee(endCrystal) && (raytrace.getValue() || endCrystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) >= MathHelper.square(breakWallsRange.getValue().doubleValue()))) return;

        breakRunnable = () -> {
            if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(Vec3d.ofCenter(crystal.getBlockPos(), 0)));
            Break(crystal);
        };
    }

    private void placeCrystals(boolean sequential) {
        PlaceTarget placeTarget = this.placeTarget == null ? null : this.placeTarget.clone();
        if (placeTarget == null || placeTarget.getPosition() == null) {
            DrugHack.getInstance().getRenderManager().setRenderPosition(null);
            return;
        }

        int slot = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (!(autoSwitch.getValue() == InventoryUtils.Switch.None) && slot == -1 && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)) return;

        BlockPos position = placeTarget.getPosition();
        DrugHack.getInstance().getRenderManager().setRenderPosition(position);

        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) >= MathHelper.square(placeRange.getValue().doubleValue())) return;
        if (!mc.world.getWorldBorder().contains(position)) return;
        if (mc.world.getBlockState(position).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(position).getBlock() != Blocks.BEDROCK) return;
        if (!mc.world.getBlockState(position.add(0, 1, 0)).isAir() || (placements.getValue() == Placements.Protocol && !mc.world.getBlockState(position.add(0, 2, 0)).isAir())) return;
        if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) >= MathHelper.square(placeWallsRange.getValue().doubleValue()))) return;
        if (mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0))).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof EndCrystalEntity))) return;
        rotations = new Vector2f(calculateRotations(Vec3d.ofCenter(position, 1))[0], calculateRotations(Vec3d.ofCenter(position, 1))[1]);
        if (rotate.getValue() == Rotate.Normal) DrugHack.getInstance().getRotationManager().addRotation(changer);
        if (!placeTimer.hasTimeElapsed(placeDelay.getValue())) return;

        if (!sequential && placedSequentially) {
            placedSequentially = false;
            return;
        }

        placeRunnable = () -> {
            boolean switched = false;

            if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(Vec3d.ofCenter(position, 1)));

            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);
                switched = true;
            }

            Place(position);

            if (switched) InventoryUtils.switchBack(autoSwitch.getValue().name(), slot, previousSlot);
        };

        if (sequential) {
            placeRunnable.run();
            placeRunnable = null;

            placedSequentially = true;
        }
    }

    private void Break(EndCrystalEntity crystal) {
        int previousSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;

        if (!(weakness.getValue() == Weakness.None) && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            int slot = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (slot != -1) {
                InventoryUtils.switchSlot(weakness.getValue().name(), slot, previousSlot);
                switched = true;
            }
        }

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        if (switched) InventoryUtils.switchBack(weakness.getValue().name(), 0, previousSlot);

        brokenCrystals.put(crystal.getId(), System.currentTimeMillis());
        breakTimer.reset();
    }

    private EndCrystalEntity calculateCrystals() {
        if (!categoryBreak.getValue().isEnabled()) return null;
        if (shouldPause("Break")) return null;

        List<PlayerEntity> players = getPlayers();
        if (players.isEmpty()) return null;

        EndCrystalEntity optimalCrystal = null;
        float optimalDamage = 0.0f;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (!crystal.isAlive()) continue;
            if (inhibit.getValue() && brokenCrystals.containsKey(entity.getId())) continue;
            if (crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(breakRange.getValue().doubleValue())) continue;
            if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) continue;
            if (!WorldUtils.canSee(crystal) && (raytrace.getValue() || crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos()) > MathHelper.square(breakWallsRange.getValue().doubleValue())))
                continue;

            if (!DrugHack.getInstance().getModuleManager().getSuicide().isToggled()) {
                float damage = DamageUtils.getCrystalDamage(mc.player, null, crystal, ignoreTerrain.getValue());
                if (damage > maxSelfDamage.getValue()) continue;
                if (antiSuicide.getValue() && damage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) continue;
            }

            boolean override = false;
            for (PlayerEntity player : players) {
                float damage = DamageUtils.getCrystalDamage(player, PositionUtils.extrapolate(player, extrapolation.getValue().intValue()), crystal, ignoreTerrain.getValue());
                if (damage < getMinDamage(player, minDamage.getValue()) && damage < player.getHealth() + player.getAbsorptionAmount() && !(damage * (1.0f + lethalMultiplier.getValue()) >= player.getHealth() + player.getAbsorptionAmount()))
                    continue;

                if (damage > optimalDamage || damage > player.getHealth() + player.getAbsorptionAmount()) {
                    optimalCrystal = crystal;
                    optimalDamage = damage;

                    if (damage > player.getHealth() + player.getAbsorptionAmount()) {
                        override = true;
                        break;
                    }
                }
            }

            if (override) break;
        }

        return optimalCrystal;
    }


    private void Place(BlockPos position) {
        Hand hand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND;
        Direction direction = WorldUtils.getClosestDirection(position, true);

        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(WorldUtils.getHitVector(position, direction), direction, position, false), 0));

        switch (swing.getValue()) {
            case Swing.Default -> mc.player.swingHand(hand);
            case Swing.Packet -> mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            case Swing.Mainhand -> mc.player.swingHand(Hand.MAIN_HAND);
            case Swing.Offhand -> mc.player.swingHand(Hand.OFF_HAND);
            case Swing.Both -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.swingHand(Hand.OFF_HAND);
            }
        }

        placedCrystals.put(position, System.currentTimeMillis());
        countedCrystals.put(position, System.currentTimeMillis());
        placeTimer.reset();
    }

    private PlaceTarget calculatePlacements(BlockPos exception) {
        if (!categoryPlace.getValue().isEnabled()) return null;

        if (shouldPause("Place") || ((autoSwitch.getValue() == InventoryUtils.Switch.None || InventoryUtils.findHotbar(Items.END_CRYSTAL) == -1) && (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)))
            return null;

        List<PlayerEntity> players = getPlayers();
        if (players.isEmpty()) return null;

        BlockPos optimalPosition = null;
        PlayerEntity optimalPlayer = null;
        List<Entity> obstructions = new ArrayList<>();
        float optimalDamage = 0.0f;

        int calculations = 0;

        for (int i = 0; i < DrugHack.getInstance().getWorldManager().getRadius(Math.max(placeRange.getValue().doubleValue(), placeWallsRange.getValue().doubleValue())); i++) {
            BlockPos position = mc.player.getBlockPos().add(DrugHack.getInstance().getWorldManager().getOffset(i));

            if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeRange.getValue().doubleValue())) continue;
            if (!mc.world.getWorldBorder().contains(position)) continue;
            if (mc.world.getBlockState(position).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(position).getBlock() != Blocks.BEDROCK) continue;
            if (!mc.world.getBlockState(position.add(0, 1, 0)).isAir() || (placements.getValue() == Placements.Protocol && !mc.world.getBlockState(position.add(0, 2, 0)).isAir())) continue;

            if (!WorldUtils.canSee(position) && (raytrace.getValue() || mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(position)) > MathHelper.square(placeWallsRange.getValue().doubleValue()))) continue;
            if (mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0))).stream().anyMatch(entity -> entity.isAlive() && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof EndCrystalEntity))) continue;

            List<Entity> obstructingCrystals = mc.world.getOtherEntities(null, new Box(position.add(0, 1, 0))).stream().filter(entity -> entity instanceof EndCrystalEntity crystal && crystal.age >= (20 - breakDelay.getValue())).toList();

            if (!DrugHack.getInstance().getModuleManager().getSuicide().isToggled()) {
                float selfDamage = DamageUtils.getCrystalDamage(mc.player, null, position, exception, ignoreTerrain.getValue());
                if (selfDamage > maxSelfDamage.getValue().floatValue()) continue;
                if (antiSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) continue;
            }

            boolean override = false;
            for (PlayerEntity player : players) {
                calculations++;

                float damage = DamageUtils.getCrystalDamage(player, PositionUtils.extrapolate(player, extrapolation.getValue()), position, exception, ignoreTerrain.getValue());
                if (damage < getMinDamage(player, minDamage.getValue().floatValue()) && damage < player.getHealth() + player.getAbsorptionAmount() && !(damage * (1.0f + lethalMultiplier.getValue().floatValue()) >= player.getHealth() + player.getAbsorptionAmount())) continue;

                if (exception == null && !obstructingCrystals.isEmpty()) {
                    obstructions.add(obstructingCrystals.getFirst());
                    break;
                }

                if (damage > optimalDamage || damage > player.getHealth() + player.getAbsorptionAmount()) {
                    optimalPosition = position;
                    optimalPlayer = player;
                    optimalDamage = damage;

                    if (damage > player.getHealth() + player.getAbsorptionAmount()) {
                        override = true;
                        break;
                    }
                }
            }

            if (override) break;
        }

        if (optimalPosition == null) return new PlaceTarget(null, null, obstructions, null, 0.0f, calculations);
        return new PlaceTarget(optimalPosition, optimalPlayer, obstructions, exception, optimalDamage, calculations);

    }

    private boolean shouldPause(String process) {
        boolean eatingFlag = (whileEating.getValue() == WhileEating.None || (process.equalsIgnoreCase("Break") && whileEating.getValue() == WhileEating.Place) || (process.equalsIgnoreCase("Place") && whileEating.getValue() == WhileEating.Break));
        return eatingFlag && mc.player.isUsingItem();
    }

    private List<PlayerEntity> getPlayers() {
        List<PlayerEntity> players = new ArrayList<>();

        if (DrugHack.getInstance().getModuleManager().getSuicide().isToggled()) {
            players.add(mc.player);
            return players;
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;
            if (mc.player.squaredDistanceTo(player) > MathHelper.square(enemyRange.getValue().doubleValue())) continue;
            if (DrugHack.getInstance().getFriendManager().contains(player.getName().getString())) continue;

            players.add(player);
        }

        return players;
    }

    private float getMinDamage(PlayerEntity player, float minimumDamage) {
        if (player == null) return minimumDamage;

        if (mc.world.getOtherEntities(null, new Box(player.getBlockPos()).expand(1)).stream().anyMatch(entity -> entity instanceof ItemEntity item && item.getStack().getItem() == Items.OBSIDIAN && item.getStack().getCount() >= 8 && item.age <= 2 + DrugHack.getInstance().getServerManager().getPingDelay() + (placeDelay.getValue())) && !mc.world.getOtherEntities(null, new Box(mc.player.getBlockPos()).expand(1)).stream().anyMatch(entity -> entity instanceof ItemEntity item && item.getStack().getItem() == Items.OBSIDIAN && item.getStack().getCount() >= 8 && item.age <= 2 + DrugHack.getInstance().getServerManager().getPingDelay() + (placeDelay.getValue()))) return 2.0f;
        if (!categoryFacePlace.getValue().isEnabled()) return minimumDamage;

        if (facePlaceTimer.hasTimeElapsed(facePlaceSpeed)) {
            if (player.getHealth() + player.getAbsorptionAmount() <= healthAmount.getValue()) return Math.min(minimumDamage, 2.0f);
            if (damageSync.getValue()) return Math.min(minimumDamage, 2.0f);

            if (categoryFacePlace.getValue().isEnabled()) {
                for (ItemStack stack : player.getArmorItems()) {
                    if (stack == null || !(stack.getItem() instanceof ArmorItem)) continue;
                    if (Math.round(((stack.getMaxDamage() - stack.getDamage()) * 100.0) / stack.getMaxDamage()) <= armorPercentage.getValue()) return Math.min(minimumDamage, 2.0f);
                }
            }
        }

        return minimumDamage;
    }

    private float[] calculateRotations(Vec3d vec3d) {
        float[] rotations = RotationUtils.getRotations(vec3d);

        if (yawStep.getValue()) {
            float yaw;
            float difference = DrugHack.getInstance().getPositionManager().getServerYaw() - rotations[0];
            if (Math.abs(difference) > 180.0f) difference += difference > 0.0f ? -360.0f : 360.0f;
            float deltaYaw = (difference > 0.0f ? -1 : 1) * yawStepThreshold.getValue().floatValue();
            if (Math.abs(difference) > yawStepThreshold.getValue().floatValue()) yaw = DrugHack.getInstance().getPositionManager().getServerYaw() + deltaYaw;
            else yaw = rotations[0];
            rotations[0] = yaw;
        }

        return rotations;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        breakRunnable = null;
        placeRunnable = null;
        DrugHack.getInstance().getRenderManager().setRenderPosition(null);
        DrugHack.getInstance().getRotationManager().removeRotation(changer);
        brokenCrystals.clear();
        placedCrystals.clear();
        countedCrystals.clear();
        brokenSequentially = false;
        placedSequentially = false;
        target = null;
        placeTarget = null;
        mineTarget = null;
        calculationDamage = "0.00";
        crystalCounter.reset();
        highestID = -100000;
    }

    @Override
    public String getDisplayInfo() {
        return target == null ? null : target.getName().getString() + ", " + calculationDamage + ", " + crystalsPerSecond;
    }

    @Getter @AllArgsConstructor
    public static class PlaceTarget {
        private BlockPos position;
        private PlayerEntity player;
        private List<Entity> obstructions;
        private BlockPos exception;
        private float damage;
        private int calculations;

        public PlaceTarget clone() {
            return new PlaceTarget(position, player, obstructions, exception, damage, calculations);
        }
    }

    @AllArgsConstructor
    public enum Weakness implements Nameable {
        None("None"),
        Normal("Normal"),
        Silent("Silent");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Placements implements Nameable {
        Native("Native"),
        Protocol("Protocol");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Sequential implements Nameable {
        None("None"),
        Strict("Strict"),
        Strong("Strong");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Rotate implements Nameable {
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
    public enum Swing implements Nameable {
        Default("Default"),
        Mainhand("Mainhand"),
        Offhand("Offhand"),
        Both("Both"),
        Packet("Packet"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum WhileEating implements Nameable {
        None("None"),
        Break("Break"),
        Place("Place"),
        Both("Both");

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
}