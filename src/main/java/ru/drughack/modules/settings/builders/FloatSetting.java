package ru.drughack.modules.settings.builders;

import lombok.Builder;
import lombok.NonNull;
import ru.drughack.modules.settings.Setting;

import java.util.function.Predicate;

@Builder(builderMethodName = "_builder")
public class FloatSetting {
    @NonNull
    private final String name;

    @Builder.Default
    private Float defaultValue = 0f;

    @Builder.Default
    private Float min = 0f;

    @Builder.Default
    private Float max = 50f;

    @Builder.Default
    private Predicate<Float> visibility = null;

    @Builder.Default
    private String description = "";

    public Setting<Float> toSetting() {
        return new Setting<>(name, defaultValue, min, max, visibility, description);
    }

    public static FloatSettingBuilder builder(String name){
        return _builder().name(name);
    }
}