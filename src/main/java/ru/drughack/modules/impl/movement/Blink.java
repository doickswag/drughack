package ru.drughack.modules.impl.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import ru.drughack.api.event.impl.EventPacketSend;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.world.NetworkUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

//ЛУЧШИЙ БЛИНК!!!!!!!!!!!!
public class Blink extends Module {

    private final Setting<Boolean> pulse = new Setting<>("Pulse", true);
    private final Setting<Float> delay =  new Setting<>("Resume Delay", 50f, 1f, 2000f);

    public Blink() {
        super("Blink", "stop your movement packet and resume", Category.Movement);
    }

    private final TimerUtils timer = new TimerUtils();
    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();

    @EventHandler
    public void onPacket(EventPacketSend e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof PlayerMoveC2SPacket) {
            packets.add(e.getPacket());
            e.cancel();
        }

        if ((e.getPacket() instanceof PlayerInteractEntityC2SPacket || e.getPacket() instanceof EntityVelocityUpdateS2CPacket) && !packets.isEmpty()) resumePackets();
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (timer.hasTimeElapsed(delay.getValue()) && pulse.getValue()) {
            if (!packets.isEmpty()) resumePackets();
            timer.reset();
        }
    }

    private void resumePackets() {
        for (Packet<?> packet : packets) NetworkUtils.sendWithoutEventPacket(packet);
        packets.clear();
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) return;
        super.onDisable();
        resumePackets();
        timer.reset();
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;
        super.onEnable();
        timer.reset();
    }
}