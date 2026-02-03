package fun.drughack.modules.settings.impl;

import fun.drughack.modules.settings.Setting;
import fun.drughack.modules.settings.api.Bind;

import java.util.function.Supplier;

public class BindSetting extends Setting<Bind> {

    public BindSetting(String name, Bind defaultValue) {
        super(name, defaultValue);
    }

    public BindSetting(String name, Bind defaultValue, Supplier<Boolean> visible) {
        super(name, defaultValue, visible);
    }
}