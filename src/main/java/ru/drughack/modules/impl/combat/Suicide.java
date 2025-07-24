package ru.drughack.modules.impl.combat;

import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventClientConnect;
import ru.drughack.api.event.impl.EventPlayerDeath;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;

public class Suicide extends Module {

    public Suicide() {
        super("Suicide", "autocrystal but mc.player", Category.Combat);
    }

    @EventHandler
    public void onPlayerDeath(EventPlayerDeath event) {
        if (event.getPlayer() != mc.player) return;
        toggle();
    }

    @EventHandler
    public void onPlayerLogin(EventClientConnect event) {
        toggle();
    }
}