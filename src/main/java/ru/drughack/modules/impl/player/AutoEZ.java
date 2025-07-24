package ru.drughack.modules.impl.player;

import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTargetDeath;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoEZ extends Module {

    private final Setting<String> text = new Setting<>("Text", "[user], ez sucker");
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    public AutoEZ() {
        super("AutoEZ", "saying you text after kill ([user] - username)", Category.Player);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (fullNullCheck() || queue.isEmpty()) return;

        synchronized (queue) {
            String message = queue.poll();
            if (message != null && !message.isEmpty()) mc.player.networkHandler.sendChatMessage(message);
        }
    }

    @EventHandler
    public void onTargetDeath(EventTargetDeath event) {
        if (fullNullCheck() || event.getPlayer() == mc.player) return;

        synchronized (queue) {
            queue.clear();
            String message = getKillMessage(event.getPlayer().getName().getString());
            queue.add(message);
        }
    }

    private String getKillMessage(String username) {
        return text.getValue().replace("[user]", username);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        queue.clear();
    }
}