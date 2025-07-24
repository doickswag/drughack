package ru.drughack.modules.impl.misc;

import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.formatting.FormattingUtils;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.math.ZeroTimerUtils;
import lombok.*;

public class FastLatency extends Module {

    public Setting<Integer> delay = new Setting<>("Delay", 100, 0, 1000);
    public Setting<Boolean> spikeNotifier = new Setting<>("SpikeNotifier", false);
    public Setting<Integer> threshold = new Setting<>("Threshold", 30, 0, 1000, v -> spikeNotifier.getValue());

    private final TimerUtils timer = new TimerUtils();
    private final ZeroTimerUtils receivedTimer = new ZeroTimerUtils();
    private long time;
    @Getter private int latency;

    public FastLatency() {
        super("FastLatency", "make your latency faster", Category.Misc);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (receivedTimer.hasTimeElapsed(1000L) && timer.hasTimeElapsed(delay.getValue().longValue())) {
            mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(1000, "/w "));
            time = System.currentTimeMillis();
            receivedTimer.reset();
            timer.reset();
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (event.getPacket() instanceof CommandSuggestionsS2CPacket packet) {
            if (packet.id() == 1000) {
                int ping = (int) (System.currentTimeMillis() - time);
                if (spikeNotifier.getValue() && ping - latency > threshold.getValue()) DrugHack.getInstance().getChatManager().message("Your ping has spiked to " + CustomFormatting.CLIENT + ping + "ms" + Formatting.WHITE + " from " + CustomFormatting.CLIENT + latency + "ms" + Formatting.WHITE + "!", "module-" + getName().toLowerCase() + "-spike");

                latency = ping;
                receivedTimer.zero();
            }
        }
    }
}