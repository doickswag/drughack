package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IClientPlayerEntity;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventMotion;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import net.minecraft.entity.effect.StatusEffects;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.MoveUtils;

public class Sprint extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Rage);

    public Sprint() {
        super("Sprint", "Sprint", Category.Movement);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (mc.player == null) return;

        if (shouldSprint()) mc.player.setSprinting(true);
    }

    @EventHandler
    public void onPlayerMove(EventMotion event) {

        if (mode.getValue() == Mode.Instant) {
            if (fullNullCheck() || DrugHack.getInstance().getModuleManager().getSpeed().isToggled()) return;
            if (DrugHack.getInstance().getServerManager().getFallDistance() >= 5.0f || mc.player.isSneaking() || mc.player.isClimbing() || mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB || mc.player.getAbilities().flying || mc.player.isGliding()) return;
            if ((mc.player.isTouchingWater() || mc.player.isInLava())) return;

            Vector2d velocity = MoveUtils.forward(MoveUtils.getPotionSpeed(MoveUtils.DEFAULT_SPEED));
            event.setMovement(new Vec3d(velocity.x, event.getMovement().getY(), event.getMovement().getZ()));
            event.setMovement(new Vec3d(event.getMovement().getX(), event.getMovement().getY(), velocity.y));
            event.cancel();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;
        mc.player.setSprinting(shouldSprint());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) return;
        mc.player.setSprinting(false);
    }

    public boolean shouldSprint() {
        if (!((IClientPlayerEntity) mc.player).invokeCanSprint()) return false;
        if (mc.player.isTouchingWater() && !mc.player.isSubmergedInWater()) return false;
        if (mc.player.isSwimming() && !mc.player.isOnGround() && !mc.player.input.playerInput.sneak() && !mc.player.isTouchingWater()) return false;

        if (mode.getValue() == Mode.Rage) {
            return mc.player.isSubmergedInWater() ? (mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward() || mc.player.input.playerInput.left() || mc.player.input.playerInput.right()) : (mc.player.input.movementForward >= 0.8 || mc.player.input.movementForward <= -0.8 || mc.player.input.movementSideways >= 0.8 || mc.player.input.movementSideways <= -0.8);
        } else if (mode.getValue() == Mode.Legit) {
            if (!((IClientPlayerEntity) mc.player).invokeIsWalking()) return false;
            if (mc.player.isUsingItem() && (!DrugHack.getInstance().getModuleManager().getNoSlow().isToggled())) return false;
            if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) return false;
            if (mc.player.isGliding()) return false;
            if (mc.player.horizontalCollision && !mc.player.collidedSoftly) return false;
            return mc.player.input.hasForwardMovement();
        } else {
            return mc.player.isSubmergedInWater() ? (mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward() || mc.player.input.playerInput.left() || mc.player.input.playerInput.right()) : (mc.player.input.movementForward >= 0.8 || mc.player.input.movementForward <= -0.8 || mc.player.input.movementSideways >= 0.8 || mc.player.input.movementSideways <= -0.8);
        }
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Rage("Rage"),
        Legit("Legit"),
        Instant("Instant");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}