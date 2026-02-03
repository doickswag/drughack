package fun.drughack.modules.impl.movement;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.combat.Aura;
import meteordevelopment.orbit.EventHandler;

public class Sprint extends Module {
	
    public Sprint() {
        super("Sprint", Category.Movement);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (DrugHack.getInstance().getModuleManager().getModule(Aura.class).isToggled()
        		&& DrugHack.getInstance().getModuleManager().getModule(Aura.class).getTarget() != null
                && DrugHack.getInstance().getModuleManager().getModule(Aura.class).sprint.getValue() == Aura.Sprint.Legit) {
        	if (mc.player.getAbilities().flying
        			|| mc.player.isRiding()
        			|| DrugHack.getInstance().getServerManager().getFallDistance() <= 0f
        			&& mc.player.isOnGround()) {
        		mc.options.sprintKey.setPressed(true);
        	} else {
                mc.options.sprintKey.setPressed(false);
                mc.player.setSprinting(false);
        	}
        } else mc.options.sprintKey.setPressed(true);
    }
}