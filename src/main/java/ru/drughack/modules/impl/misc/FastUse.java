package ru.drughack.modules.impl.misc;

import ru.drughack.api.mixins.accesors.IMinecraftClient;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;

public class FastUse extends Module {

    public FastUse() {
        super("FastUse", "makes your femboy hand faster", Category.Misc);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        ((IMinecraftClient) mc).setUseCooldown(0);
    }
}