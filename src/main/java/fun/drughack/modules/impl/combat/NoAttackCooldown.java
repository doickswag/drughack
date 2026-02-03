package fun.drughack.modules.impl.combat;

import fun.drughack.api.events.impl.EventTick;
import fun.drughack.api.mixins.accessors.IMinecraftClient;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import meteordevelopment.orbit.EventHandler;

public class NoAttackCooldown extends Module {

    public NoAttackCooldown() {
        super("NoAttackCooldown", Category.Combat);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        ((IMinecraftClient) mc).setAttackCooldown(0);
    }
}