package ru.drughack.modules.impl.player;

import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventKeyboardTick;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import net.minecraft.util.PlayerInput;
import org.joml.Vector2d;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.world.MoveUtils;

public class Freecam extends Module {

    public Setting<Float> horizontalSpeed = new Setting<>("HorizontalSpeed", 1.0f, 0.1f, 3.0f);
    public Setting<Float> verticalSpeed = new Setting<>("VerticalSpeed", 0.5f, 0.1f, 3.0f);

    public Freecam() {
        super("Freecam", "camera but free", Category.Player);
    }

    private float freeYaw, freePitch;
    private float prevFreeYaw, prevFreePitch;
    private double freeX, freeY, freeZ;
    private double prevFreeX, prevFreeY, prevFreeZ;

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        prevFreeYaw = freeYaw;
        prevFreePitch = freePitch;
        freeYaw = mc.player.getYaw();
        freePitch = mc.player.getPitch();
    }

    @EventHandler
    public void onKeyboardTick(EventKeyboardTick event) {
        if (mc.player == null || mc.world == null) return;
        Vector2d motion = MoveUtils.forward(horizontalSpeed.getValue().doubleValue());
        prevFreeX = freeX;
        prevFreeY = freeY;
        prevFreeZ = freeZ;
        freeX += motion.x;
        freeZ += motion.y;

        if (mc.options.jumpKey.isPressed()) freeY += verticalSpeed.getValue().doubleValue();
        if (mc.options.sneakKey.isPressed()) freeY -= verticalSpeed.getValue().doubleValue();

        mc.player.input.playerInput = new PlayerInput(mc.player.input.playerInput.forward(), mc.player.input.playerInput.backward(), mc.player.input.playerInput.left(), mc.player.input.playerInput.right(), false, false, false);
        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null || mc.world == null) {
            setToggled(false);
            return;
        }

        mc.chunkCullingEnabled = false;

        freeYaw = prevFreeYaw = mc.player.getYaw();
        freePitch = prevFreePitch = mc.player.getPitch();

        freeX = prevFreeX = mc.player.getX();
        freeY = prevFreeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        freeZ = prevFreeZ = mc.player.getZ();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null || mc.world == null) return;
        mc.chunkCullingEnabled = true;
    }

    public float getFreeYaw() {
        return (float) MathUtils.interpolate(prevFreeYaw, freeYaw, mc.getRenderTickCounter().getTickDelta(true));
    }

    public float getFreePitch() {
        return (float) MathUtils.interpolate(prevFreePitch, freePitch, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFreeX() {
        return MathUtils.interpolate(prevFreeX, freeX, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFreeY() {
        return MathUtils.interpolate(prevFreeY, freeY, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFreeZ() {
        return MathUtils.interpolate(prevFreeZ, freeZ, mc.getRenderTickCounter().getTickDelta(true));
    }
}