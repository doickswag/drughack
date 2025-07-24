package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class HighJump extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Funtime);
    public Setting<Float> boost = new Setting<>("Boost", 0.6f, 0.1f, 1f);

    public HighJump() {
        super("HighJump", "jumps highs", Category.Movement);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (mc.player.isOnGround() && (mc.world.getBlockState(mc.player.getBlockPos()).getBlock() instanceof CarpetBlock || mc.world.getBlockState(mc.player.getBlockPos()).getBlock() instanceof SnowBlock)) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, mc.player.getBlockPos().up(), Direction.UP));
            mc.player.jump();
            mc.player.setVelocity(mc.player.getVelocity().x, boost.getValue(), mc.player.getVelocity().z);
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Funtime("Funtime");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}