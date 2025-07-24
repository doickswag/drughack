package ru.drughack.modules.settings.builders;

import lombok.Builder;
import lombok.NonNull;
import ru.drughack.modules.settings.Setting;

import java.util.function.Predicate;

@Builder(builderMethodName = "_builder")
public class BooleanSetting {
    @NonNull
    private final String name;

    @Builder.Default
    private Boolean defaultValue = false;
    @Builder.Default
    private Predicate<Boolean> visibility = null;
    @Builder.Default
    private String description = "";

    public Setting<Boolean> toSetting(){
        return new Setting<>(name, defaultValue, false, false, visibility, description);
    }

    public static BooleanSettingBuilder builder(String name){
        return _builder().name(name);
    }
}