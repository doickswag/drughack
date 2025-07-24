package ru.drughack.modules.impl.misc;

import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class AutoReconnect extends Module {

    public Setting<Integer> delay = new Setting<>("Delay", 5, 0, 20);

    public AutoReconnect() {
        super("AutoReconnect", "??? ??? ???", Category.Misc);
    }
}