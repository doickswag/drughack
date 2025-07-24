package ru.drughack.modules.impl.misc;

import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class NameProtect extends Module {

    public Setting<String> name = new Setting<>("Name", "drughack.cc");

    public NameProtect() {
        super("NameProtect", "Protect your name", Category.Misc);
    }
}