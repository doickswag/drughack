package ru.drughack.modules.settings;

import lombok.*;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventChangeSetting;
import ru.drughack.modules.settings.api.*;
import ru.drughack.modules.settings.impl.*;

import java.util.function.Predicate;

@Getter @Setter
public class Setting<T> {
    private final String name;
    private final T defaultValue;
    private T value;
    private T plannedValue;
    private T min;
    private T max;
    private boolean number;
    private Predicate<T> visibility;
    private String description = "";

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
    }

    public Setting(String name, T defaultValue, String description, Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.description = description;
        this.visibility = visibility;
    }

    public Setting(String name, T defaultValue, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.plannedValue = defaultValue;
        this.description = description;
    }

    public Setting(String name, T defaultValue, T min, T max, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.description = description;
        this.number = true;
    }

    public Setting(String name, T defaultValue, T min, T max) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.number = true;
    }

    public Setting(String name, T defaultValue, T min, T max, Predicate<T> visibility, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.description = description;
        this.number = true;
    }

    public Setting(String name, T defaultValue, T min, T max, Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.plannedValue = defaultValue;
        this.visibility = visibility;
        this.number = true;
    }

    public Setting(String name, T defaultValue, Predicate<T> visibility) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visibility = visibility;
        this.plannedValue = defaultValue;
    }

    private void setValueInternal(T value, boolean checkLimits) {
        T finalValue = value;
        if (checkLimits && this.number && this.min instanceof Number) {
            float floatValue = ((Number) value).floatValue();
            float minValue = ((Number) this.min).floatValue();
            float maxValue = ((Number) this.max).floatValue();

            if (floatValue < minValue) {
                finalValue = this.min;
            } else if (floatValue > maxValue) {
                finalValue = this.max;
            }
        }

        EventChangeSetting event = new EventChangeSetting(this);
        DrugHack.getInstance().getEventHandler().post(event);

        if (!event.isCanceled()) {
            this.value = finalValue;
            this.plannedValue = finalValue;
        }
    }

    public void setValue(T value) {
        setValueInternal(value, true);
    }

    public void setEnumValue(String value) {
        for (Enum<?> e : ((Enum<?>) this.value).getClass().getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value) || (e instanceof Nameable && ((Nameable) e).getName().equalsIgnoreCase(value))) {
                setValueInternal((T) e, false);
                break;
            }
        }
    }

    public void reset() {
        setValueInternal(getDefaultValue(), false);
    }

    public void increaseEnum() {
        Enum<?> newValue = EnumConverter.increaseEnum((Enum<?>) this.value);
        setValueInternal((T) newValue, false);
    }

    public String currentEnumName() {
        if (this.value instanceof Nameable) {
            return ((Nameable) this.value).getName();
        } else if (this.value instanceof Enum<?>) {
            return ((Enum<?>) this.value).name();
        }
        return this.value.toString();
    }

    public boolean isNumberSetting() {
        return this.value instanceof Double || this.value instanceof Integer || this.value instanceof Short || this.value instanceof Long || this.value instanceof Float;
    }

    public boolean isColorSetting() {
        return this.value instanceof ColorSetting;
    }

    public boolean isCategorySetting() {
        return this.value instanceof CategorySetting;
    }

    public boolean isBooleanSetting() {
        return this.value instanceof Boolean;
    }

    public boolean isBindSetting() {
        return this.value instanceof Bind;
    }

    public boolean isCharacterSetting() {
        return this.value instanceof Character;
    }

    public boolean isStringSetting() {
        return this.value instanceof String;
    }

    public boolean isCategoryBooleanSetting() {
        return this.value instanceof CategoryBooleanSetting;
    }

    public boolean isNameSetting() {
        return this.value instanceof Enum<?> || this.value instanceof Nameable;
    }

    public boolean isVisible() {
        if (this.visibility == null) return true;
        return this.visibility.test(this.getValue());
    }
}