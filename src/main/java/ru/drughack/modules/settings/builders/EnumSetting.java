package ru.drughack.modules.settings.builders;

import lombok.Builder;
import lombok.NonNull;
import ru.drughack.modules.settings.Setting;

import java.util.function.Predicate;

@Builder(builderMethodName = "_builder", builderClassName = "EnumSettingBuilder")
public class EnumSetting<E extends Enum<E>> {
    @NonNull
    private final String name;

    @NonNull
    private final E defaultValue;

    @Builder.Default
    private Predicate<E> visibility = null;

    @Builder.Default
    private String description = "";

    public Setting<E> toSetting() {
        return new Setting<>(name, defaultValue, defaultValue, defaultValue, visibility, description);
    }

    public static <E extends Enum<E>> EnumSettingBuilder<E> builder(String name, E defaultValue) {
        return EnumSetting.<E>_builder()
                .name(name)
                .defaultValue(defaultValue);
    }
}