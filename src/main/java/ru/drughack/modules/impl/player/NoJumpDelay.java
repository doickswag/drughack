package ru.drughack.modules.impl.player;

import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.api.mixins.accesors.ILivingEntity;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "off the delay for jump", Category.Player);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;
        ((ILivingEntity) mc.player).setJumpingCooldown(0);
    }
}