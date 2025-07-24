package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventDisconnect;
import ru.drughack.api.event.impl.EventPacketSend;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.api.mixins.accesors.ILivingEntity;
import ru.drughack.api.mixins.accesors.IPlayerMoveC2SPacket;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class NoFall extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Plain);

    public NoFall() {
        super("NoFall", "canceled the fall packet for ignore damage", Category.Movement);
    }

    private int takenFallDamage = 0;
    private boolean started = false;
    private boolean skipTick = true;

    @EventHandler
    public void onPacketReceive(EventPacketSend e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
            switch (mode.getValue()) {
                case Plain -> ((IPlayerMoveC2SPacket) packet).setOnGround(true);
                case Grim -> {
                    if (started) ((IPlayerMoveC2SPacket) packet).setOnGround(false);
                }
            }
        }
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) {
            started = false;
            skipTick = true;
            return;
        }

        if (mode.getValue() == Mode.Grim) {
            if (!mc.player.isOnGround() && DrugHack.getInstance().getServerManager().getFallDistance() > 3 && !started) started = true;

            if (started) {
                mc.options.jumpKey.setPressed(false);
                if (mc.player.isOnGround()) {
                    if (skipTick) {
                        skipTick = false;
                        return;
                    }
                    mc.player.jump();
                    ((ILivingEntity) mc.player).setJumpingCooldown(10);
                    started = false;
                    skipTick = true;
                    updateFallDamage();
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(EventDisconnect e) {
        takenFallDamage = 0;
    }

    public void updateFallDamage() {
        if (takenFallDamage < 5) takenFallDamage++;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        takenFallDamage = 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        takenFallDamage = 0;
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Plain("Plain"),
        Grim("Grim");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}