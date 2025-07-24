package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.drughack.api.mixins.accesors.IClientPlayerEntity;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventAttackEntity;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class Criticals extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);

    public Criticals() {
        super("Criticals", "made your hits criticals", Category.Combat);
    }

    @EventHandler
    public void onAttackEntity(EventAttackEntity event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getTarget() == null || event.getTarget() instanceof EndCrystalEntity) return;

        if (mc.player.isOnGround() || mc.player.getAbilities().flying || mode.getValue() == Mode.Grim && !mc.player.isInLava() && !mc.player.isSubmergedInWater()) {
            switch (mode.getValue()) {
                case Mode.Packet -> {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.05, mc.player.getZ(), false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.03, mc.player.getZ(), false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision));
                }

                case Mode.Strict -> {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.11, mc.player.getZ(), false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.1100013579, mc.player.getZ(), false, mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0000013579, mc.player.getZ(), false, mc.player.horizontalCollision));
                }

                case Mode.Grim -> {
                    if (!mc.player.isOnGround()) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.000001, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, mc.player.horizontalCollision));
                    }
                }
            }

            ((IClientPlayerEntity) mc.player).setLastOnGround(false);
            mc.player.addCritParticles(event.getTarget());
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().name();
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Packet("Packet"),
        Strict("Strict"),
        Grim("Grim");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}