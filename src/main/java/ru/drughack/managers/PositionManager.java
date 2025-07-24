package ru.drughack.managers;

import lombok.Getter;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPacketSend;
import ru.drughack.utils.interfaces.Wrapper;

@Getter
public class PositionManager implements Wrapper {

    public PositionManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    private float serverYaw, serverPitch;
    private double serverX, serverY, serverZ;
    private boolean serverOnGround, serverSprinting, serverSneaking;
    private int serverSlot;

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesPosition()) {
                serverX = packet.getX(mc.player.getX());
                serverY = packet.getY(mc.player.getY());
                serverZ = packet.getZ(mc.player.getZ());
            }

            if (packet.changesLook()) {
                serverYaw = packet.getYaw(mc.player.getYaw());
                serverPitch = packet.getPitch(mc.player.getPitch());
            }

            serverOnGround = packet.isOnGround();
        }

        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) serverSlot = packet.getSelectedSlot();

        if (event.getPacket() instanceof ClientCommandC2SPacket packet) {
            switch (packet.getMode()) {
                case START_SPRINTING -> serverSprinting = true;
                case STOP_SPRINTING -> serverSprinting = false;
                case PRESS_SHIFT_KEY -> serverSneaking = true;
                case RELEASE_SHIFT_KEY -> serverSneaking = false;
            }
        }
    }
}