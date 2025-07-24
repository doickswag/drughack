package ru.drughack.managers;

import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventCameraRotate;
import ru.drughack.api.event.impl.EventKeyboardTick;
import ru.drughack.api.event.impl.EventPlayerChangeLook;
import ru.drughack.modules.api.Module;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.rotations.RotationChanger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static ru.drughack.utils.interfaces.Wrapper.mc;

public class RotationManager {

    private final List<RotationChanger> rotationChangers = new CopyOnWriteArrayList<>();
    @Getter private final RotationData rotationData = new RotationData();

    public RotationManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    public void addRotation(RotationChanger changer) {
        if (Module.fullNullCheck()) return;

        if (!rotationChangers.contains(changer)) {
            rotationChangers.add(changer);
            sortRotations();

            rotationData.setRotation(mc.gameRenderer.getCamera().getYaw(), mc.gameRenderer.getCamera().getPitch());
        }
    }

    public void addPacketRotation(float[] rotations) {
        addPacketRotation(rotations[0], rotations[1]);
    }

    public void addPacketRotation(float yaw, float pitch) {
        if (Module.fullNullCheck()) return;
        if (DrugHack.getInstance().getPositionManager().getServerYaw() == yaw || DrugHack.getInstance().getPositionManager().getServerPitch() == pitch) return;
        removeRotation(rotationChangers.getFirst());
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                DrugHack.getInstance().getPositionManager().getServerX(),
                DrugHack.getInstance().getPositionManager().getServerY(),
                DrugHack.getInstance().getPositionManager().getServerZ(),
                yaw,
                pitch,
                DrugHack.getInstance().getPositionManager().isServerOnGround(),
                mc.player.horizontalCollision
        ));
    }

    public void removeRotation(RotationChanger changer) {
        if (Module.fullNullCheck()) return;

        if (rotationChangers.contains(changer)) {
            rotationChangers.remove(changer);
            sortRotations();

            mc.player.setYaw(mc.gameRenderer.getCamera().getYaw());
            mc.player.setPitch(mc.gameRenderer.getCamera().getPitch());
        }
    }

    public boolean isEmpty() {
        return rotationChangers.isEmpty();
    }

    public boolean containsRotation(RotationChanger changer) {
        return rotationChangers.contains(changer);
    }

    private void sortRotations() {
        rotationChangers.sort(Comparator.comparing(RotationChanger::priority));
        Collections.reverse(rotationChangers);
    }

    @EventHandler
    public void onCameraRotate(EventCameraRotate e) {
        if (Module.fullNullCheck() || rotationChangers.isEmpty()) return;

        e.setRotation(new Vec2f(rotationData.getYaw(), rotationData.getPitch()));
    }

    @EventHandler
    public void onPlayerLookChange(EventPlayerChangeLook.Pre e) {
        if (Module.fullNullCheck() || rotationChangers.isEmpty()) return;
        Float[] rotations = rotationChangers.getFirst().rotations().get();

        if (rotations != null && rotations.length >= 2) {
            float yaw = rotations[0];
            float pitch = rotations[1];
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }

    @EventHandler
    public void onPlayerLookChange$POST(EventPlayerChangeLook.Post e) {
        if (Module.fullNullCheck() || rotationChangers.isEmpty()) return;

        e.cancel();
        rotationData.updateRotation(e.getCursorDeltaX(), e.getCursorDeltaY());
    }

    @EventHandler
    public void onKeyboardTick(EventKeyboardTick e) {
        if (!rotationChangers.isEmpty() && !Module.fullNullCheck()) if (DrugHack.getInstance().getModuleManager().getAntiCheat().movementSync.getValue()) moveFix();
    }

    private void moveFix() {
        if (Module.fullNullCheck()) return;
        float forward = (mc.player.input.playerInput.forward() ? 1 : mc.player.input.playerInput.backward() ? -1 : 0);
        float sideways = (mc.player.input.playerInput.left() ? 1 : mc.player.input.playerInput.right() ? -1 : 0);
        Matrix4f matrix = new Matrix4f();
        matrix.rotate((float) Math.toRadians(mc.player.getYaw() - mc.gameRenderer.getCamera().getYaw()), 0, 1, 0);
        Vec3d updatedInput = MathUtils.transformPos(matrix, sideways, 0, forward);
        forward = (float) (Math.round(updatedInput.getZ())) * (mc.player.isSneaking() ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);
        sideways = (float) (Math.round(updatedInput.getX())) * (mc.player.isSneaking() ? (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED) : 1);
        setForward(forward > 0.0f);
        setBackward(forward < 0.0f);
        setLeft(sideways > 0.0f);
        setRight(sideways < 0.0f);
        mc.player.input.movementForward = forward;
        mc.player.input.movementSideways = sideways;
    }

    public static void setForward(boolean forward) {
        PlayerInput input = mc.player.input.playerInput;
        mc.player.input.playerInput = new PlayerInput(forward, input.backward(), input.left(), input.right(), input.jump(), input.sneak(), input.sprint());
    }

    public static void setBackward(boolean backward) {
        PlayerInput input = mc.player.input.playerInput;
        mc.player.input.playerInput = new PlayerInput(input.forward(), backward, input.left(), input.right(), input.jump(), input.sneak(), input.sprint());
    }

    public static void setLeft(boolean left) {
        PlayerInput input = mc.player.input.playerInput;
        mc.player.input.playerInput = new PlayerInput(input.forward(), input.backward(), left, input.right(), input.jump(), input.sneak(), input.sprint());
    }

    public static void setRight(boolean right) {
        PlayerInput input = mc.player.input.playerInput;
        mc.player.input.playerInput = new PlayerInput(input.forward(), input.backward(), input.left(), right, input.jump(), input.sneak(), input.sprint());
    }

    @Getter
    public static class RotationData {
        private float yaw, prevYaw;
        private float pitch, prevPitch;

        public void updateRotation(double deltaX, double deltaY) {
            prevYaw = yaw;
            prevPitch = pitch;
            yaw += (float) deltaX * 0.15f;
            pitch += (float) deltaY * 0.15f;
            pitch = MathHelper.clamp(pitch, -90f, 90f);

            while (yaw >= 360f) yaw -= 360f;
            while (yaw < 0f) yaw += 360f;
        }

        public void setRotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}