package fun.drughack.modules.impl.client;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.screen.clickgui.ClickGui;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.api.Bind;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class UI extends Module {

    public UI() {
        super("UI", Category.Client);
        setBind(new Bind(GLFW.GLFW_KEY_RIGHT_SHIFT, false));
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (!(mc.currentScreen instanceof ClickGui)) setToggled(false);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.setScreen(DrugHack.getInstance().getClickGui());
    }
}