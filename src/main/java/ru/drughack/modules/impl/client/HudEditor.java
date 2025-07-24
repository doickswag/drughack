package ru.drughack.modules.impl.client;

import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.gui.HudEditorScreen;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.utils.other.CursorUtils;

public class HudEditor extends Module {

    public HudEditor() {
        super("HudEditor", "Editor for a hud", Category.Client);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DrugHack.getInstance().getModuleManager().getUi().toggle();
        mc.setScreen(DrugHack.getInstance().getHudEditorScreen());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (fullNullCheck()) return;
        mc.setScreen(null);
        CursorUtils.setCursor(CursorUtils.ARROW);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;
        if (!(mc.currentScreen instanceof HudEditorScreen)) toggle();
    }
}