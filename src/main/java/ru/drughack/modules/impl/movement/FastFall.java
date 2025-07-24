package ru.drughack.modules.impl.movement;

import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class FastFall extends Module {

    public Setting<Float> height = new Setting<>("Height", 3.0f, 0.0f, 12.0f);

    public FastFall() {
        super("FastFall", "Reverse Step", Category.Movement);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (!DrugHack.getInstance().getServerManager().getSetbackTimer().hasTimeElapsed(300L)) return;
        if (mc.player.isRiding() || mc.player.isGliding() || mc.player.isHoldingOntoLadder() || mc.player.isInLava() || mc.player.isTouchingWater() || mc.player.input.playerInput.jump() || mc.player.input.playerInput.sneak()) {
            return;
        }

        if (mc.player.isOnGround() && nearBlock(height.getValue().doubleValue())) {
            mc.player.setVelocity(mc.player.getVelocity().getX(), -height.getValue().doubleValue(), mc.player.getVelocity().getZ());
        }
    }

    private boolean nearBlock(double height) {
        for (double i = 0; i < height + 0.5; i += 0.01) {
            if (!mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0, -i, 0))) return true;
        }

        return false;
    }

    @Override
    public String getDisplayInfo() {
        return height.getValue().toString();
    }
}