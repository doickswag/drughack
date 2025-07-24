package ru.drughack.modules.impl.render;

import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class Swing extends Module {

    public Setting<Float> speed = new Setting<>("Speed", 1f, 0f, 2.5f);

    public Swing() {
        super("Swing", "change you swing", Category.Render);
    }
}