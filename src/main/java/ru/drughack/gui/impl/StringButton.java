package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.drughack.DrugHack;
import ru.drughack.modules.settings.Setting;
import ru.drughack.gui.api.Button;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class StringButton extends Button {
    private final Setting<String> setting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");
    private float textOffsetX = 0f;
    private final TimerUtils animationTimer = new TimerUtils();
    private final TimerUtils pauseTimer = new TimerUtils();
    private boolean isMovingRight = false;
    private boolean isWaiting = true;

    public StringButton(Setting<String> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) output = str.substring(0, str.length() - 1);
        return output;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (getState()) Renderer2D.renderQuad(context.getMatrices(), this.x, this.y, this.x + this.width, this.y + this.height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));
        String other = System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        String text = isListening ? currentString.string() + other : setting.getName() + ": " + setting.getValue();
        float textWidth = DrugHack.getInstance().getFontManager().getWidth(isListening ? currentString.string() + "_" : setting.getName() + ": " + setting.getValue());
        boolean isLong = (textWidth > this.width - 5f) && isHovering(mouseX, mouseY);

        if (isLong) {
            if (isWaiting) {
                if (pauseTimer.hasTimeElapsed(500)) {
                    isWaiting = false;
                    pauseTimer.reset();
                }
            } else if (animationTimer.hasTimeElapsed(20)) {
                float maxOffset = textWidth - (this.width - 5f);
                if (isMovingRight) {
                    textOffsetX += 0.5f;
                    if (textOffsetX >= maxOffset) {
                        textOffsetX = maxOffset;
                        isMovingRight = false;
                        isWaiting = true;
                        pauseTimer.reset();
                    }
                } else {
                    textOffsetX -= 0.5f;
                    if (textOffsetX <= 0) {
                        textOffsetX = 0;
                        isMovingRight = true;
                        isWaiting = true;
                        pauseTimer.reset();
                    }
                }
                animationTimer.reset();
            }
        } else {
            textOffsetX = 0f;
            isMovingRight = true;
            isWaiting = true;
        }

        context.enableScissor((int) this.x + 1, (int) this.y, (int) (this.x + this.width), (int) (this.y + this.height));
        float textX = this.x + 2.5f - (isLong ? textOffsetX : 0f);
        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, text, textX, this.y - 1.7f + 4.5f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            if (isListening) CursorUtils.setCursor(CursorUtils.IBEAM);
            else CursorUtils.setCursor(CursorUtils.HAND);
            DrugHack.getInstance().getClickGuiScreen().anyHover = true;
            DrugHack.getInstance().getHudEditorScreen().anyHover = true;
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, text, textX, this.y - 1.7f + 4.5f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        }
        context.disableScissor();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY) && mouseButton == 0) {
            this.isListening = !this.isListening;
            if (this.isListening) this.currentString = new CurrentString(this.setting.getValue());
            else this.enterString();
            playSound("click");
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.isListening) this.setString(this.currentString.string() + typedChar);
    }

    @Override
    public void onKeyPressed(int key) {
        if (isListening) {
            switch (key) {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE:
                    this.enterString();
                    this.isListening = false;
                    break;
                case GLFW.GLFW_KEY_BACKSPACE:
                    this.setString(removeLastChar(this.currentString.string()));
                    break;
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    private void enterString() {
        this.setting.setValue(this.currentString.string());
        this.setString(this.currentString.string());
    }

    @Override
    public boolean getState() {
        return this.isListening;
    }

    public void setString(String newString) {
        this.currentString = new CurrentString(newString);
    }

    public record CurrentString(String string) { }
}