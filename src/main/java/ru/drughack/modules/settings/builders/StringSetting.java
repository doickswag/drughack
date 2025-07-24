package ru.drughack.modules.settings.builders;

import lombok.Builder;
import lombok.NonNull;
import ru.drughack.modules.settings.Setting;

import java.util.function.Predicate;

@Builder(builderMethodName = "_builder")
public class StringSetting {
    @NonNull
    private final String name;

    @Builder.Default
    private String defaultValue = "";
    @Builder.Default
    private Predicate<String> visibility = null;
    @Builder.Default
    private String description = "";

    public Setting<String> toSetting(){
        return new Setting<>(name, defaultValue, description, visibility);
    }

    public static StringSetting.StringSettingBuilder builder(String name){
        return _builder().name(name);
    }
}