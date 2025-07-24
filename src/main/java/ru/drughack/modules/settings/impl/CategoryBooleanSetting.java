package ru.drughack.modules.settings.impl;

import lombok.*;

@Getter @Setter
public class CategoryBooleanSetting {
    public boolean open;
    public boolean enabled;

    public CategoryBooleanSetting(boolean enabled) {
        this.enabled = enabled;
    }
}