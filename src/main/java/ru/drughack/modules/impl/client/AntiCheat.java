package ru.drughack.modules.impl.client;

import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class AntiCheat extends Module {

    public Setting<Boolean> movementSync = new Setting<>("Movement Sync", false);

    public AntiCheat() {
        super("AntiCheat", "configuration of rotate/movement", Category.Client);
        toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (fullNullCheck()) return;
        toggle();
    }
}