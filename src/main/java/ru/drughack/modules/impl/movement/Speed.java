package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventJumpPlayer;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.mixins.accesors.ILivingEntity;
import ru.drughack.api.mixins.accesors.IVec3d;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventMotion;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.MoveUtils;

public class Speed extends Module {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Strafe);
    private final Setting<Float> speed = new Setting<>("Speed", 1.2f, 1f, 2f, v -> mode.getValue() == Mode.Collision);
    private final Setting<Boolean> useTimer = new Setting<>("Use Timer", false, v -> mode.getValue() == Mode.Strafe);
    private final Setting<Boolean> timerBypass = new Setting<>("Timer Bypass", false, v -> mode.getValue() == Mode.Strafe);
    private final Setting<Float> timerMultiplier = new Setting<>("Timer Multiplier", 1.08f, 1f, 1.5f, v -> useTimer.getValue());
    private final Setting<Float> bypassThreshold = new Setting<>("Bypass Threshold", 25f, 15f, 30f, v -> timerBypass.getValue());
    private final Setting<Boolean> speedInWater = new Setting<>("Speed In Water", false);
    private double distance, forward;
    private int ticks, stage, jumps;

    public Speed() {
        super("Speed", "boost your speed", Category.Movement);
    }

    @EventHandler
    public void onJumpPlayer(EventJumpPlayer e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Intave && jumps++ >= 32) {
            DrugHack.getInstance().getChatManager().error("Toggled off to avoid the flag.", "jumps-error");
            setToggled(false);
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Intave && e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            DrugHack.getInstance().getChatManager().error("Flag detected. Toggled off...", "flags-error");
            setToggled(false);
        }
    }

    @EventHandler
    public void onMotion(EventMotion event) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Intave) {
            for (int i = 0; i < 10; i++) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

            if (mc.player.getVelocity().y > 0
                    && mc.player.getVelocity().y < 0.10f
                    || DrugHack.getInstance().getServerManager().getFallDistance() > 0.3f
                    && DrugHack.getInstance().getServerManager().getFallDistance() < 0.4f
            ) mc.player.setVelocity(mc.player.getVelocity().x * 1.07f, mc.player.getVelocity().y, mc.player.getVelocity().z * 1.07f);
        }

        if (mode.getValue() == Mode.Strafe || mode.getValue() == Mode.StrafeStrict) {
            if (DrugHack.getInstance().getServerManager().getFallDistance() >= 5.0f || mc.player.isSneaking() || mc.player.isClimbing() || mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB || mc.player.getAbilities().flying || (mc.player.isInFluid() && !speedInWater.getValue())) return;
            double speed = MoveUtils.getPotionSpeed(MoveUtils.DEFAULT_SPEED) * (mc.player.input.movementForward <= 0 && forward > 0 ? 0.66 : 1);

            if (stage == 1 && MoveUtils.isMoving() && mc.player.verticalCollision) {
                ((IVec3d) mc.player.getVelocity()).setY(MoveUtils.getPotionJump(0.3999999463558197));
                event.setMovement(new Vec3d(event.getMovement().getX(), mc.player.getVelocity().getY(), event.getMovement().getZ()));
                speed *= 2.149;
                stage = 2;
            } else if (stage == 2) {
                speed = distance - (0.66 * (distance - MoveUtils.getPotionSpeed(MoveUtils.DEFAULT_SPEED)));
                stage = 3;
            } else {
                if (!mc.world.getEntityCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)).isEmpty() || mc.player.verticalCollision) stage = 1;
                speed = distance - distance / 159.0;
            }

            speed = Math.max(speed, MoveUtils.getPotionSpeed(MoveUtils.DEFAULT_SPEED));
            double ncp = MoveUtils.getPotionSpeed(mode.getValue() == Mode.StrafeStrict || mc.player.input.movementForward < 1 ? 0.465 : 0.576);
            double bypass = MoveUtils.getPotionSpeed(mode.getValue() == Mode.StrafeStrict || mc.player.input.movementForward < 1 ? 0.44 : 0.57);
            speed = Math.min(speed, ticks > 25 ? ncp : bypass);
            if (ticks++ > 50) ticks = 0;
            Vector2d velocity = MoveUtils.forward(speed);
            event.setMovement(new Vec3d(velocity.x, event.getMovement().getY(), event.getMovement().getZ()));
            event.setMovement(new Vec3d(event.getMovement().getX(), event.getMovement().getY(), velocity.y));
            forward = mc.player.input.movementForward;
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Collision) if (isCollision()) mc.player.setVelocity(mc.player.getVelocity().x * speed.getValue(), mc.player.getVelocity().y, mc.player.getVelocity().z * speed.getValue());
        
        if (mode.getValue() == Mode.Strafe || mode.getValue()== Mode.StrafeStrict) {
            distance = Math.sqrt(MathHelper.square(mc.player.getX() - mc.player.prevX) + MathHelper.square(mc.player.getZ() - mc.player.prevZ));
            boolean flag = MoveUtils.isMoving() && !mc.player.isSneaking() && !mc.player.isInFluid() && DrugHack.getInstance().getServerManager().getFallDistance() < 5.0f;
            DrugHack.getInstance().getWorldManager().setTimerMultiplier(useTimer.getValue() && flag && (ticks > bypassThreshold.getValue().intValue() || !timerBypass.getValue()) ? timerMultiplier.getValue().floatValue() : 1.0f);
        }
    }

    private boolean isCollision() {
        for (Entity entity : mc.world.getEntities())
            if (entity instanceof LivingEntity && entity != mc.player)
                if (mc.player.getBoundingBox().intersects(entity.getBoundingBox()))
                    return true;

        return false;
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Collision("Grim"),
        Intave("Intave"),
        Strafe("Strafe"),
        StrafeStrict("Strict");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @Override
    public void onEnable() {
        ticks = 0;
        jumps = 0;
        stage = 1;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f);
        super.onDisable();
    }
}