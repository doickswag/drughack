package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import ru.drughack.DrugHack;
import ru.drughack.modules.settings.Setting;
import ru.drughack.gui.api.Button;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class NameButton extends Button {
    public Setting<Enum<?>> setting;
    private boolean open = false;

    public NameButton(Setting<Enum<?>> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Renderer2D.renderQuad(context.getMatrices(), this.x, this.y, this.x + this.width, this.y + this.height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));

        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, setting.getName() + ": " + this.setting.currentEnumName(), x + 2.3f, y + 2.3f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            CursorUtils.setCursor(CursorUtils.HAND);
            DrugHack.getInstance().getClickGuiScreen().anyHover = true;
            DrugHack.getInstance().getHudEditorScreen().anyHover = true;
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, setting.getName() + ": " + this.setting.currentEnumName(), x + 2.3f, y + 2.3f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        }

        if (open) {
            float yOffset = height + 1.5f;
            for (Enum<?> enumZZZ : setting.getValue().getClass().getEnumConstants()) {
                boolean selected = enumZZZ == setting.getValue();
                if (selected) Renderer2D.renderQuad(context.getMatrices(), this.x, this.y + yOffset, this.x + this.width, this.y + yOffset + height, ColorUtils.getColor(
                        DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                        (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
                ));
                if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y + yOffset && mouseY <= this.y + yOffset + height) {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, ((Nameable) enumZZZ).getName(), (this.x + 2.6f) - (float) DrugHack.getInstance().getFontManager().getWidth(((Nameable) enumZZZ).getName()) / 2 + this.width / 2f, this.y + yOffset + (height / 2f) - 3.5f - 1f, -1);
                } else {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, ((Nameable) enumZZZ).getName(), (this.x + 2.6f) - (float) DrugHack.getInstance().getFontManager().getWidth(((Nameable) enumZZZ).getName()) / 2 + this.width / 2f, this.y + yOffset + (height / 2f) - 3.5f, -1);
                }
                yOffset += (height + 1.5f);
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            if (mouseButton == 0) {
                this.setting.increaseEnum();
                playSound("click");
            } else if (mouseButton == 1) {
                open = !open;
                playSound("rclick");
            }
        }

        if (open && mouseButton == 0) {
            float yOffset = height + 1.5f;
            for (Enum<?> enumValue : setting.getValue().getClass().getEnumConstants()) {
                if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y + yOffset && mouseY <= this.y + yOffset + height) {
                    setting.setValue(enumValue);
                    playSound("click");
                    break;
                }
                yOffset += (height + 1.5f);
            }
        }
    }

    @Override
    public float getHeight() {
        return this.height + (open ? setting.getValue().getClass().getEnumConstants().length * (height + 1.5f) : 0);
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + height;
    }

    @Override
    public boolean getState() {
        return true;
    }
}