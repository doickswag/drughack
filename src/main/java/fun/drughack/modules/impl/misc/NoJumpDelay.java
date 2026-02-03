package fun.drughack.modules.impl.misc;

import fun.drughack.api.events.impl.EventTick;
import fun.drughack.api.mixins.accessors.ILivingEntity;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import meteordevelopment.orbit.EventHandler;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", Category.Misc);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        ((ILivingEntity) mc.player).setJumpingCooldown(0);
    }
}