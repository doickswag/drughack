package ru.drughack.modules.settings.builders;

import lombok.Builder;
import lombok.NonNull;
import ru.drughack.modules.settings.impl.Bind;
import ru.drughack.modules.settings.Setting;

import java.util.function.Predicate;

@Builder(builderMethodName = "_builder")
public class BindSetting {
    @NonNull
    private final String name;

    @Builder.Default
    private Bind defaultValue = new Bind(-1, false, false);
    @Builder.Default
    private Predicate<Bind> visibility = null;
    @Builder.Default
    private String description = "";

    public Setting<Bind> toSetting(){
        return new Setting<>(name, defaultValue, description, visibility);
    }

    public static BindSetting.BindSettingBuilder builder(String name){
        return _builder().name(name);
    }
}