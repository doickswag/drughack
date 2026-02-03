package fun.drughack.modules.impl.movement;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.NumberSetting;

public class ElytraForward extends Module {

    public final NumberSetting forward = new NumberSetting("settings.elytraforward.forward", 3f, 1f, 6f, 1f);

    public ElytraForward() {
        super("ElytraForward", Category.Movement);
    }
}