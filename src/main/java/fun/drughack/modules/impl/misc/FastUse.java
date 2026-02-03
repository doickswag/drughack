package fun.drughack.modules.impl.misc;

import fun.drughack.api.events.impl.EventTick;
import fun.drughack.api.mixins.accessors.IMinecraftClient;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import meteordevelopment.orbit.EventHandler;

public class FastUse extends Module {

    public FastUse() {
        super("FastUse", Category.Misc);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        ((IMinecraftClient) mc).setItemUseCooldown(0);
    }
}