package ru.drughack.modules.impl.misc;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.*;
import ru.drughack.api.mixins.accesors.IMinecraftClient;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.rotations.RotationChanger;
import ru.drughack.utils.rotations.RotationUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.MoveUtils;
import ru.drughack.utils.world.NetworkUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Phase extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Pearl);
    //pearl
    public Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.Normal, v -> mode.getValue() == Mode.Pearl);
    public Setting<Boolean> attack = new Setting<>("Attack", true, v -> mode.getValue() == Mode.Pearl);
    public Setting<Integer> pitch = new Setting<>("Pitch", 85, 60, 90, v -> mode.getValue() == Mode.Pearl);
    public Setting<Boolean> swing = new Setting<>("Swing", true, v -> mode.getValue() == Mode.Pearl);
    //walk
    public Setting<Boolean> edgeEnable = new Setting<>("Edge Enable", false, v -> mode.getValue() == Mode.Walk);
    public Setting<ModeV2> modeV2 = new Setting<>("ModeV2", ModeV2.Clip, v -> mode.getValue() == Mode.Walk);
    public Setting<Float> delay = new Setting<>("Delay", 200f, 0f, 1000f, v -> mode.getValue() == Mode.Walk);
    public Setting<Integer> attempts = new Setting<>("Attempts", 5, 0, 10, v -> mode.getValue() == Mode.Walk);
    public Setting<Boolean> cancelPlayer = new Setting<>("Cancel", true, v -> mode.getValue() == Mode.Walk);
    public Setting<HandleTeleport> handleTeleport = new Setting<>("Handle Teleport", HandleTeleport.All, v -> mode.getValue() == Mode.Walk);
    public Setting<Boolean> autoSpeed = new Setting<>("AutoSpeed", true, v -> mode.getValue() == Mode.Walk);
    public Setting<Float> speed = new Setting<>("Speed", 3f, 1f, 10f, v -> mode.getValue() == Mode.Walk && !autoSpeed.getValue());

    public Phase() {
        super("Phase", "allows you to move around in blocks", Category.Misc);
    }
    private Vector2f rotations = new Vector2f(0, 0);
    private final RotationChanger changer = new RotationChanger(500, () -> new Float[]{rotations.getX(), rotations.getY()});
    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    private final TimerUtils timer = new TimerUtils();
    private boolean cancel = false;
    private int ticks;

    @EventHandler
    public void onPacketSend(EventPacketSend e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime) {
            packets.add(e.getPacket());
            e.cancel();
        }
    }

    @EventHandler
    public void onPacket(EventPacket e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Walk) {
            if (e.getPacket() instanceof PlayerActionC2SPacket && cancel && cancelPlayer.getValue()) e.cancel();
            if (e.getPacket() instanceof TeleportConfirmC2SPacket && handleTeleport.getValue() == HandleTeleport.Cancel) e.cancel();
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
                int teleportID = packet.teleportId();
                if (handleTeleport.getValue() == HandleTeleport.All) {
                    mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportID - 1));
                    mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportID));
                    mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportID + 1));
                }

                if (handleTeleport.getValue() == HandleTeleport.Below) mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportID + -1));
                if (handleTeleport.getValue() == HandleTeleport.Above) mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportID + 1));

                if (handleTeleport.getValue() == HandleTeleport.NoBand) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0.0D, 1337.0D, 0.0D, mc.player.isOnGround(), mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportID + 1));
                }
            }
        }
    }

    @EventHandler
    public void onRenderer3D(EventRender3D e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime) Renderer3D.renderCube(e.getMatrices(), mc.player.getBoundingBox(), true, ColorUtils.getGlobalColor(50), true, ColorUtils.getGlobalColor());
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime) {
            ticks++;
            if (BlockPos.stream(mc.player.getBoundingBox().expand(-1e-3)).noneMatch(s -> mc.world.getBlockState(s).isSolid()) && ticks > 5) setToggled(false);
        }

        if (mode.getValue() == Mode.Walk) {
            mc.player.setVelocity(0, 0, 0);
            if (modeV2.getValue() == ModeV2.Clip) {
                if (shouldPacket()) {
                    if (timer.hasTimeElapsed(delay.getValue())) {
                        double[] forward = MoveUtils.forward$(getSpeed());
                        for (int i = 0; i < attempts.getValue(); ++i) sendPackets(mc.player.getX() + forward[0], mc.player.getY() + getUpMovement(), mc.player.getZ() + forward[1]);
                        timer.reset();
                    }
                } else cancel = false;
            }
        }
    }

    @EventHandler
    public void onMotion(EventMotion e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Walk) {
            if (shouldPacket()) {
                if (modeV2.getValue() == ModeV2.Smooth) {
                    double[] forward = MoveUtils.forward$(getSpeed());
                    for (int i = 0; i < attempts.getValue(); ++i) sendPackets(mc.player.getX() + forward[0], mc.player.getY() + getUpMovement(), mc.player.getZ() + forward[1]);
                    e.setMovement(new Vec3d(0.0D, e.getY(), e.getZ()));
                    if (mc.options.jumpKey.isPressed()) e.setMovement(new Vec3d(e.getX(), 0.05D, e.getZ()));
                    else e.setMovement(new Vec3d(e.getX(), 0.0D, e.getZ()));
                    e.setMovement(new Vec3d(e.getX(), e.getY(), 0.0));
                    e.cancel();
                }
            }
        }
    }

    @EventHandler
    public void onBlockCollision(EventBlockCollision e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime)
            if (e.getBlockPos().getY() >= mc.player.getBlockY() || mc.options.sneakKey.isPressed()) e.setState(Blocks.AIR.getDefaultState());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mode.getValue() == Mode.Funtime) {
            packets.forEach(NetworkUtils::sendWithoutEventPacket);
            packets.clear();
            ticks = 0;
            NetworkUtils.sendWithoutEventPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;
        super.onEnable();

        if (mode.getValue() == Mode.Pearl) {
            if (!mc.world.getBlockState(mc.player.getBlockPos()).isReplaceable()) {
                toggle();
                return;
            }

            if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
                toggle();
                return;
            }

            int slot = InventoryUtils.find(Items.ENDER_PEARL, 0, 8);
            int previousSlot = mc.player.getInventory().selectedSlot;

            if (slot == -1) {
                toggle();
                return;
            }

            float yaw = Math.round(RotationUtils.getRotations(new Vec3d(Math.floor(mc.player.getX()) + 0.5, 0, Math.floor(mc.player.getZ()) + 0.5))[0]) + 180;
            float prevYaw = mc.player.getYaw();
            float prevPitch = mc.player.getPitch();
            rotations = new Vector2f(yaw, pitch.getValue());
            if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(yaw, pitch.getValue());
            if (rotate.getValue() == Rotate.Normal) DrugHack.getInstance().getRotationManager().addRotation(changer);
            InventoryUtils.switchSlot("Silent", slot, previousSlot);
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch.getValue()));
            if (swing.getValue()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            if (attack.getValue()) ((IMinecraftClient) mc).attack();
            InventoryUtils.switchBack("Silent", slot, previousSlot);
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(prevYaw, prevPitch, mc.player.isOnGround(), mc.player.horizontalCollision));
            toggle();
        }
    }

    private void sendPackets(double x, double y, double z) {
        cancel = false;
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(0.0D, 1337.0D, 0.0D, mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
        cancel = true;
    }

    private boolean shouldPacket() {
        return !edgeEnable.getValue() || mc.player.horizontalCollision;
    }

    private double getUpMovement() {
        return (double) (mc.options.jumpKey.isPressed() ? 1 : (mc.options.sneakKey.isPressed() ? -1 : 0)) * getSpeed();
    }

    private double getSpeed() {
        return autoSpeed.getValue() ? MoveUtils.getPotionSpeed(MoveUtils.DEFAULT_SPEED) : speed.getValue().doubleValue() / 10.0D;
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Pearl("Pearl"),
        Funtime("Funtime"),
        Walk("Walk");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    private enum ModeV2 implements Nameable {
        Clip("Clip"),
        Smooth("Smooth");

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
    private enum HandleTeleport implements Nameable {
        All("All"),
        Below("Below"),
        Above("Above"),
        NoBand("NoBand"),
        Last("Last"),
        Cancel("Cancel"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}