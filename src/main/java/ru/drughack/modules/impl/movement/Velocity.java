package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.*;
import ru.drughack.api.mixins.accesors.IEntityVelocityUpdateS2CPacket;
import ru.drughack.api.mixins.accesors.IVec3d;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.WorldUtils;

public class Velocity extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    public Setting<Integer> ticks = new Setting<>("Ticks", 5, 0, 20, v -> mode.getValue() == Mode.JumpReset);
    public Setting<Float> reduce = new Setting<>("Reduce", 0.5f, 0f, 1f, v -> mode.getValue() == Mode.Reduce);
    public Setting<Integer> count = new Setting<>("Count", 3, 0, 10, v -> mode.getValue() == Mode.Reduce);
    public Setting<Integer> horizontal = new Setting<>("Horizontal", 0, 0, 100, v -> mode.getValue() == Mode.Normal);
    public Setting<Integer> vertical = new Setting<>("Vertical", 0, 0, 100, v -> mode.getValue() == Mode.Normal);
    public Setting<Boolean> explosions = new Setting<>("Explosions", true);
    public Setting<Boolean> pause = new Setting<>("Pause", true, v -> mode.getValue() == Mode.GrimOld || mode.getValue() == Mode.Cancel);
    public Setting<Boolean> antiPush = new Setting<>("AntiPush", true);
    public Setting<Boolean> antiLiquidPush = new Setting<>("AntiLiquidPush", false);
    public Setting<Boolean> antiBlockPush = new Setting<>("AntiBlockPush", true);
    public Setting<Boolean> antiFishingRod = new Setting<>("AntiFishingRod", false);

    public Velocity() {
        super("Velocity", "Rejects packets for velocity", Category.Movement);
    }

    private boolean oldCancel, fallDamage;
    private long lastAttackTime = 0L;
    private int limitUntilJump = 0;
    private int attacks = 0;

    @EventHandler
    public void onTick(EventTick event) {
        if (fullNullCheck()) return;
        if (mode.getValue() != Mode.GrimOld) return;
        if (!oldCancel) return;

        if (!pause.getValue() || DrugHack.getInstance().getServerManager().getSetbackTimer().hasTimeElapsed(100L)) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), DrugHack.getInstance().getPositionManager().getServerYaw(), DrugHack.getInstance().getPositionManager().getServerPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
        }

        oldCancel = false;
    }

    @EventHandler
    public void onAttackEntity(EventAttackEntity e) {
        if (mode.getValue() == Mode.Reduce) {
            if (mc.player.hurtTime > 0 && ++attacks % count.getValue() == 0 && System.currentTimeMillis() - lastAttackTime <= 8000) {
                mc.player.setVelocity(new Vec3d(
                        mc.player.getVelocity().x * reduce.getValue(),
                        mc.player.getVelocity().y,
                        mc.player.getVelocity().z * reduce.getValue()
                ));
            }

            lastAttackTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.JumpReset && (mc.player.hurtTime != 9 || !mc.player.isOnGround() || !mc.player.isSprinting() || fallDamage || !(limitUntilJump >= ticks.getValue()))) {
            limitUntilJump++;
            mc.player.jump();
            return;
        }

        limitUntilJump = 0;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (fullNullCheck()) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() != mc.player.getId()) return;
            if (mode.getValue() == Mode.None) return;
            if (mode.getValue() == Mode.Walls && !isPhased()) return;

            switch (mode.getValue()) {
                case Mode.Normal, Mode.Walls -> {
                    ((IEntityVelocityUpdateS2CPacket) packet).setVelocityX((int) (((packet.getVelocityX() / 8000.0 - mc.player.getVelocity().x) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().x * 8000));
                    ((IEntityVelocityUpdateS2CPacket) packet).setVelocityY((int) (((packet.getVelocityY() / 8000.0 - mc.player.getVelocity().y) * (vertical.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().y * 8000));
                    ((IEntityVelocityUpdateS2CPacket) packet).setVelocityZ((int) (((packet.getVelocityZ() / 8000.0 - mc.player.getVelocity().z) * (horizontal.getValue().doubleValue() / 100.0)) * 8000 + mc.player.getVelocity().z * 8000));
                }

                case Mode.Cancel -> {
                    if (pause.getValue() && !DrugHack.getInstance().getServerManager().getSetbackTimer().hasTimeElapsed(100L)) return;
                    event.cancel();
                }

                case Mode.GrimOld -> {
                    if (pause.getValue() && !DrugHack.getInstance().getServerManager().getSetbackTimer().hasTimeElapsed(100L)) return;
                    event.cancel();
                    oldCancel = true;
                }

                case Mode.JumpReset -> {
                    double velocityX = packet.getVelocityX() / 8000;
                    double velocityY = packet.getVelocityY() / 8000;
                    double velocityZ = packet.getVelocityZ() / 8000;
                    fallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY == -0.078375;
                }
            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet && explosions.getValue()) {
            if (mode.getValue() == Mode.None) return;
            if (mode.getValue() == Mode.Walls && !isPhased()) return;

            switch (mode.getValue()) {
                case Mode.Normal, Mode.Walls -> {
                    if (packet.playerKnockback().isPresent()) ((IVec3d) packet.playerKnockback().get()).setX((float) (packet.playerKnockback().get().getX() * (horizontal.getValue().doubleValue() / 100.0)));
                    if (packet.playerKnockback().isPresent()) ((IVec3d) packet.playerKnockback().get()).setY((float) (packet.playerKnockback().get().getY() * (vertical.getValue().doubleValue() / 100.0)));
                    if (packet.playerKnockback().isPresent()) ((IVec3d) packet.playerKnockback().get()).setZ((float) (packet.playerKnockback().get().getZ() * (horizontal.getValue().doubleValue() / 100.0)));
                }

                case Mode.Cancel -> {
                    if (pause.getValue() && !DrugHack.getInstance().getServerManager().getSetbackTimer().hasTimeElapsed(100L)) return;
                    event.cancel();
                }

                case Mode.GrimOld -> {
                    if (pause.getValue() && !DrugHack.getInstance().getServerManager().getSetbackTimer().hasTimeElapsed(100L)) return;
                    event.cancel();
                    oldCancel = true;
                }
            }

            if (event.isCanceled()) {
                mc.executeSync(() -> {
                    Vec3d vec3d = packet.center();
                    mc.world.playSound(vec3d.getX(), vec3d.getY(), vec3d.getZ(), packet.explosionSound().value(), SoundCategory.BLOCKS, 4.0F, (1.0F + (mc.world.random.nextFloat() - mc.world.random.nextFloat()) * 0.2F) * 0.7F, false);
                    mc.world.addParticle(packet.explosionParticle(), vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1.0, 0.0, 0.0);
                });
            }
        }
    }

    private boolean isPhased() {
        return WorldUtils.getBoxes(mc.player.getBoundingBox()).stream().anyMatch(blockPos -> !mc.world.getBlockState(blockPos).isReplaceable());
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        attacks = 0;
        limitUntilJump = 0;
        lastAttackTime = 0L;
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Normal("Normal"),
        Cancel("Cancel"),
        GrimOld("GrimOld"),
        JumpReset("Jump Reset"),
        Reduce("Reduce"),
        Walls("Walls"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}