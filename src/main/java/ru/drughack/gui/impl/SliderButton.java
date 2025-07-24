package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.drughack.DrugHack;
import ru.drughack.modules.settings.Setting;
import ru.drughack.gui.api.Button;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class SliderButton extends Button {
    private final Number min;
    private final Number max;
    private float sliderPosition;
    private boolean typing, drag;
    private String valueStr = "";
    public Setting<Number> setting;

    public SliderButton(Setting<Number> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
        this.min = setting.getMin();
        this.max = setting.getMax();
        this.sliderPosition = calculateSliderPosition();
    }

    private String removeLastChar(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, input.length() - 1);
    }

    private float calculateSliderPosition() {
        return ((this.setting.getValue().floatValue() - this.min.floatValue()) / (this.max.floatValue() - this.min.floatValue()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (drag) setSettingFromX(mouseX);
        float targetPosition = calculateSliderPosition();
        sliderPosition = MathUtils.lerp(sliderPosition, targetPosition, 30 * delta);
        float sliderX = this.x + sliderPosition * (this.width);
        Renderer2D.renderQuad(context.getMatrices(), this.x, this.y, sliderX, this.y + this.height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));
        String value = typing ? valueStr :
                String.format("%.2f", this.setting.getValue().doubleValue())
                        .replace(",", ".")
                        .replaceAll("(\\.\\d)0$", "$1");

        String display = typing ? (value + (System.currentTimeMillis() % 1000 > 500 ? "_" : "")) : (setting.getName() + ": " + value);

        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, display, x + 2.3f, y + 2.3f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            CursorUtils.setCursor(CursorUtils.HAND);
            DrugHack.getInstance().getClickGuiScreen().anyHover = true;
            DrugHack.getInstance().getHudEditorScreen().anyHover = true;
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, display, x + 2.3f, y + 2.3f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            if (mouseButton == 0) {
                drag = !drag;
                if (drag) setSettingFromX(mouseX);
            } else if (mouseButton == 1) {
                typing = !typing;
                if (typing) valueStr = String.valueOf(setting.getValue());
            } else if (mouseButton == 2) {
                this.setting.setValue(setting.getDefaultValue());
                valueStr = String.valueOf(setting.getDefaultValue());
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (!typing) return;

        if (Character.isDigit(typedChar) || typedChar == '.') {
            valueStr += typedChar;
            try {
                if (setting.getValue() instanceof Float) {
                    float value = Float.parseFloat(valueStr);
                    if (value >= min.floatValue() && value <= max.floatValue()) setting.setValue(value);
                } else if (setting.getValue() instanceof Double) {
                    double value = Double.parseDouble(valueStr);
                    if (value >= min.doubleValue() && value <= max.doubleValue()) setting.setValue(value);
                } else if (setting.getValue() instanceof Integer) {
                    int value = Integer.parseInt(valueStr);
                    if (value >= min.intValue() && value <= max.intValue()) setting.setValue(value);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (!typing) return;

        switch (key) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE -> typing = false;
            case GLFW.GLFW_KEY_BACKSPACE -> {
                valueStr = removeLastChar(valueStr);
                try {
                    if (!valueStr.isEmpty()) {
                        if (setting.getValue() instanceof Float) setting.setValue(Float.parseFloat(valueStr));
                        else if (setting.getValue() instanceof Double) setting.setValue(Double.parseDouble(valueStr));
                        else if (setting.getValue() instanceof Integer) setting.setValue(Integer.parseInt(valueStr));
                    } else setting.setValue(min);
                } catch (Exception ignored) {
                    setting.setValue(min);
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        super.mouseReleased(mouseX, mouseY, releaseButton);
        if (releaseButton == 0) drag = false;
    }

    private void setSettingFromX(int mouseX) {
        float percent = Math.max(0, Math.min(1, ((float) mouseX - this.x) / this.width));

        if (this.setting.getValue() instanceof Double) {
            double range = max.doubleValue() - min.doubleValue();
            double value = min.doubleValue() + (range * percent);
            value = Math.round(value * 10.0) / 10.0;
            this.setting.setValue(value);
        } else if (this.setting.getValue() instanceof Float) {
            float range = max.floatValue() - min.floatValue();
            float value = min.floatValue() + (range * percent);
            value = Math.round(value * 10.0f) / 10.0f;
            this.setting.setValue(value);
        } else if (this.setting.getValue() instanceof Integer) {
            int range = max.intValue() - min.intValue();
            int value = min.intValue() + (int)(range * percent);
            this.setting.setValue(value);
        }

        valueStr = String.valueOf(setting.getValue());
    }
}