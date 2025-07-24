package ru.drughack.modules.settings.builders;

import lombok.Builder;
import lombok.NonNull;
import ru.drughack.modules.settings.Setting;

import java.util.function.Predicate;

@Builder(builderMethodName = "_builder")
public class IntSetting {
    @NonNull
    private final String name;

    @Builder.Default
    private Integer defaultValue = 0;

    @Builder.Default
    private Integer min = 0;

    @Builder.Default
    private Integer max = 50;

    @Builder.Default
    private Predicate<Integer> visibility = null;

    @Builder.Default
    private String description = "";

    public Setting<Integer> toSetting() {
        return new Setting<>(name, defaultValue, min, max, visibility, description);
    }

    public static IntSettingBuilder builder(String name) {
        return _builder().name(name);
    }
}