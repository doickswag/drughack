package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class CategoryBooleanButton extends Button {
    private final Setting<CategoryBooleanSetting> setting;
    private float animatedWidth = 0;
    private long lastAnimationTime = 0;

    public CategoryBooleanButton(Setting<CategoryBooleanSetting> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateAnimation();

        Renderer2D.renderQuad(context.getMatrices(), this.x, this.y, this.x + this.animatedWidth, this.y + this.height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));

        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, setting.getName(), x + 2.3f, y + 2.3f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            CursorUtils.setCursor(CursorUtils.HAND);
            DrugHack.getInstance().getClickGuiScreen().anyHover = true;
            DrugHack.getInstance().getHudEditorScreen().anyHover = true;
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, setting.getName(), x + 2.3f, y + 2.3f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        }

        if (!setting.getValue().isOpen()) {
            if (isHovering(mouseX, mouseY)) {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "+", x + width - 10, y + 4 - 1.2f, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            } else {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "+", x + width - 10, y + 4, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            }
        } else {
            if (isHovering(mouseX, mouseY)) {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "-", x + width - 10, y + 4 - 1.2f, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            } else {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "-", x + width - 10, y + 4, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            }
        }
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (this.lastAnimationTime == 0) this.lastAnimationTime = currentTime;
        float deltaTime = (currentTime - this.lastAnimationTime) / 1000.0f;
        this.lastAnimationTime = currentTime;
        float targetWidth = this.getState() ? this.width : 0;
        this.animatedWidth = MathUtils.lerp$(this.animatedWidth, targetWidth, deltaTime * 8.0f);
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            if (mouseButton == 1) {
                setting.getValue().setOpen(!setting.getValue().isOpen());
                playSound("rclick");
            } else if (mouseButton == 0) {
                setting.getValue().setEnabled(!setting.getValue().isEnabled());
                playSound("click");
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public boolean getState() {
        return this.setting.getValue().isEnabled();
    }
}