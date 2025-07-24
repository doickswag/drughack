package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventBlockCollision;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.event.impl.EventPacketSend;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.math.TimerUtils;

public class Spider extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Funtime);
    public Setting<Float> boost = new Setting<>("Boost", 0.6f, 0.1f, 1f);

    public Spider() {
        super("Spider", "you jumps like spider lol", Category.Movement);
    }

    private final TimerUtils timer = new TimerUtils();
    private boolean shouldJump = false;

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime) {
            if (shouldJump) {
                mc.player.setOnGround(true);
                mc.player.setVelocity(mc.player.getVelocity().x, boost.getValue(), mc.player.getVelocity().z);
                shouldJump = false;
            }
        }
    }

    @EventHandler
    public void onPacketSend(EventPacketReceive e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime && e.getPacket() instanceof PlayerPositionLookS2CPacket && timer.hasTimeElapsed(600)) {
            shouldJump = true;
            timer.reset();
        }
    }

    @EventHandler
    public void onBlockCollision(EventBlockCollision e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.Funtime && e.getBlockPos().getY() >= mc.player.getBlockY() && mc.player.horizontalCollision && timer.hasTimeElapsed(600))
            e.setState(Blocks.AIR.getDefaultState());
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Funtime("Funtime");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}