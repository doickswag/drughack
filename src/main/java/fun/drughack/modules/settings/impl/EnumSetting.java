package fun.drughack.modules.settings.impl;

import fun.drughack.modules.settings.Setting;
import fun.drughack.modules.settings.api.EnumConverter;
import fun.drughack.modules.settings.api.Nameable;

import java.util.function.Supplier;

public class EnumSetting<Value extends Enum<?>> extends Setting<Value> {

    public EnumSetting(String name, Value defaultValue) {
        super(name, defaultValue);
    }

    public EnumSetting(String name, Value defaultValue, Supplier<Boolean> visible) {
        super(name, defaultValue, visible);
    }

    public void increaseEnum() {
        setValue((Value) EnumConverter.increaseEnum(value));
    }

    public String currentEnumName() {
        return ((Nameable) value).getName();
    }

    public void setEnumValue(String value) {
        for (Value e : (Value[]) this.value.getClass().getEnumConstants()) {
            if (((Nameable) e).getName().equalsIgnoreCase(value)) {
                setValue(e);
                break;
            }
        }
    }
}