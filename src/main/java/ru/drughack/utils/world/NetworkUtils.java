package ru.drughack.utils.world;

import ru.drughack.utils.interfaces.Wrapper;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.*;
import ru.drughack.api.mixins.accesors.IClientWorld;
import ru.drughack.utils.mixins.IClientConnection;

public class NetworkUtils implements Wrapper {

    public static void sendSequencedPacket(SequencedPacketCreator creator) {
        try (PendingUpdateManager pendingUpdateManager = ((IClientWorld) mc.world).invokeGetPendingUpdateManager().incrementSequence();) {
            Packet<ServerPlayPacketListener> packet = creator.predict(pendingUpdateManager.getSequence());
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public static void sendWithoutEventPacket(Packet<?> packet) {
        ((IClientConnection) mc.getNetworkHandler().getConnection()).sendPacket(packet);
    }
}