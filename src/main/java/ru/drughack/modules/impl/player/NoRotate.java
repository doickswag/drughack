package ru.drughack.modules.impl.player;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IPlayerPosition;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.world.PositionUtils;

public class NoRotate extends Module {

    public Setting<Boolean> inBlocks = new Setting<>("InBlocks", false);
    public Setting<Boolean> spoof = new Setting<>("Spoof", false);

    public NoRotate() {
        super("NoRotate", "rotate but no", Category.Player);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.world == null) return;
        if (!inBlocks.getValue() && !mc.world.getBlockState(PositionUtils.getFlooredPosition(mc.player)).isReplaceable()) return;

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            if (spoof.getValue()) {
                DrugHack.getInstance().getRotationManager().addPacketRotation(packet.change().yaw(), packet.change().pitch());
                DrugHack.getInstance().getRotationManager().addPacketRotation(mc.player.getYaw(), mc.player.getPitch());
            }

            ((IPlayerPosition) (Object) packet.change()).setYaw(mc.player.getYaw());
            ((IPlayerPosition) (Object) packet.change()).setPitch(mc.player.getPitch());

            packet.relatives().remove(PositionFlag.X_ROT);
            packet.relatives().remove(PositionFlag.Y_ROT);
        }
    }
}