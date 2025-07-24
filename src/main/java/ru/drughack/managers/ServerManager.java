package ru.drughack.managers;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.event.impl.EventServerConnect;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Module;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.TimerUtils;

import java.util.Arrays;

@Getter
public class ServerManager implements Wrapper {
    private final TimerUtils setbackTimer = new TimerUtils();
    private final TimerUtils responseTimer = new TimerUtils();

    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long lastUpdate = -1;
    private long timeJoined;
    @Setter
    private float fallDistance = 0;

    private Pair<ServerAddress, ServerInfo> lastConnection;

    public ServerManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (!Module.fullNullCheck() && !mc.player.isOnGround()) {
            double y = mc.player.getY() - mc.player.prevY;
            if (y < 0) fallDistance -= (float) y;
            else fallDistance = 0;
        } else fallDistance = 0;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        responseTimer.reset();

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) setbackTimer.reset();

        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            tickRates[nextIndex] = Math.clamp(20.0f / ((System.currentTimeMillis() - lastUpdate) / 1000.0F), 0.0f, 20.0f);
            nextIndex = (nextIndex + 1) % tickRates.length;
            lastUpdate = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onClientConnect(EventPacketReceive event) {
        Arrays.fill(tickRates, 0);
        nextIndex = 0;
        timeJoined = System.currentTimeMillis();
        lastUpdate = System.currentTimeMillis();
    }

    @EventHandler
    public void onServerConnect(EventServerConnect event) {
        lastConnection = new ObjectObjectImmutablePair<>(event.getAddress(), event.getInfo());
    }

    public float getTickRate() {
        if (mc.player == null) return 0;
        if (System.currentTimeMillis() - timeJoined < 4000) return 20;
        int ticks = 0;
        float tickRates = 0.0f;

        for (float tickRate : this.tickRates) {
            if (tickRate > 0) {
                tickRates += tickRate;
                ticks++;
            }
        }

        return tickRates / ticks;
    }

    public int getPingDelay() {
        return (int) (getPing() / 25.0f);
    }

    public int getPing() {
        if (DrugHack.getInstance().getModuleManager().getFastLatency().isToggled()) return DrugHack.getInstance().getModuleManager().getFastLatency().getLatency();
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    public String getServerBrand() {
        if (mc.getCurrentServerEntry() == null || mc.getNetworkHandler() == null || mc.getNetworkHandler().getBrand() == null) return "Vanilla";
        return mc.getNetworkHandler().getBrand();
    }

    public String getServer() {
        return mc.isInSingleplayer() ? "Singleplayer" : ServerAddress.parse(mc.getCurrentServerEntry().address).getAddress();
    }
}