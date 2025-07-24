package ru.drughack.modules.impl.render;

import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class ViewClip extends Module {

    public Setting<Boolean> extend = new Setting<>("Extend", false);
    public Setting<Float> distance = new Setting<>("Distance", 3.5f, 0f, 10f, v -> extend.getValue());

    public ViewClip() {
        super("ViewClip", "clip", Category.Render);
    }

    @Override
    public String getDisplayInfo() {
        return extend.getValue() ? String.valueOf(distance.getValue().floatValue()) : "Vanilla";
    }
}