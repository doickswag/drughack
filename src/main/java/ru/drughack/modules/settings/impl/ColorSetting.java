package ru.drughack.modules.settings.impl;

import lombok.*;
import ru.drughack.DrugHack;
import ru.drughack.utils.render.ColorUtils;

import java.awt.*;

@Getter @Setter
public class ColorSetting {
    private Colors value;
    private final Colors defaultValue;

    public ColorSetting(Colors value) {
        this.value = value;
        this.defaultValue = new Colors(value.getColor(), value.isSync());
    }

    public Color getColor() {
        if (isSync() && value != DrugHack.getInstance().getModuleManager().getUi().color.getValue().getValue()) {
            return ColorUtils.getGlobalColor(getAlpha());
        } else {
            ColorUtils.getColor(value.getColor(), 255);
            return value.getColor();
        }
    }

    public void setColor(Color color) {
        value.setColor(color);
    }

    public int getAlpha() {
        return getValue().getColor().getAlpha();
    }

    public boolean isSync() {
        return value.isSync();
    }

    public void setSync(boolean sync) {
        value.setSync(sync);
    }

    public void resetValue() {
        this.value = new Colors(defaultValue.getColor(), defaultValue.isSync());
    }

    @Getter @Setter @AllArgsConstructor
    public static class Colors {
        private Color color;
        private boolean sync;
    }
}