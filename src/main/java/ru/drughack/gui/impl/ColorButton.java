package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class ColorButton extends Button {
    public final Setting<ColorSetting> setting;
    private boolean open = false;
    private boolean hoveringHue = false, hoveringColor = false, hoveringAlpha = false, hoveringCopy = false, hoveringPaste = false, hoveringSync = false;
    private boolean draggingHue = false, draggingColor = false, draggingAlpha = false;
    private float[] hsb;

    public ColorButton(Setting<ColorSetting> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
        updateHSB();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        Color outlineColor = Color.BLACK, realColor = Color.getHSBColor(hsb[0], 1, 1);
        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, setting.getName(), x + 2.3f, y + 2.3f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, setting.getName(), x + 2.3f, y + 2.3f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        }

        Renderer2D.renderQuad(context.getMatrices(), getX() + getWidth() - 10, getY() + 2, getX() + getWidth()  - 2, getY() + height - 3, ColorUtils.getColor(setting.getValue().getColor(), 255));
        if (open) {
            float offset = height;
            int colorWidth = 92;
            int dragX = (int) MathHelper.clamp(mouseX - getX() - 1, 0, colorWidth);
            int dragY = (int) MathHelper.clamp(mouseY - getY() - offset, 0, colorWidth);
            float dragHue = colorWidth * hsb[0];
            float dragSaturation = colorWidth * hsb[1];
            float dragBrightness = colorWidth * (1.0f - hsb[2]);
            float dragAlpha = colorWidth * (setting.getValue().getAlpha() / 255.0f);

            for (float i = 0; i < colorWidth; i += 0.5f) {
                Renderer2D.renderQuad(context.getMatrices(), getX() + getWidth() - 9, getY() + offset + i, getX() + getWidth() - 1, getY() + offset + i + 0.5f, Color.getHSBColor(i / colorWidth, 1.0f, 1.0f));
            }

            Renderer2D.renderOutline(context.getMatrices(), getX() + getWidth() - 9, getY() + offset, getX() + getWidth() - 1, getY() + offset + colorWidth, outlineColor);
            hoveringHue = isHoveringComponent(mouseX, mouseY, getX() + getWidth() - 9, getY() + offset, getX() + getWidth() - 1, getY() + offset + colorWidth);
            Renderer2D.renderQuad(context.getMatrices(), getX() + getWidth() - 10, getY() + offset + dragHue - 1.5f, getX() + getWidth(), getY() + offset + dragHue + 1.5f, outlineColor);

            if (draggingHue) {
                hsb[0] = (float) dragY / colorWidth;
                setColor(hsb);
            }

            Renderer2D.renderSidewaysGradient(context.getMatrices(), getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + colorWidth, Color.WHITE, realColor);
            Renderer2D.renderGradient(context.getMatrices(), getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + colorWidth, new Color(0, 0, 0, 0), Color.BLACK);
            Renderer2D.renderOutline(context.getMatrices(), getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + colorWidth, outlineColor);
            hoveringColor = isHoveringComponent(mouseX, mouseY, getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + colorWidth);
            Renderer2D.renderQuad(context.getMatrices(), getX() + 1 + dragSaturation - 1.5f, getY() + offset + dragBrightness - 1.5f, getX() + 1 + dragSaturation + 1.5f, getY() + offset + dragBrightness + 1.5f, outlineColor);
            Renderer2D.renderQuad(context.getMatrices(), getX() + 1 + dragSaturation - 0.5f, getY() + offset + dragBrightness - 0.5f, getX() + 1 + dragSaturation + 0.5f, getY() + offset + dragBrightness + 0.5f, Color.WHITE);

            if (draggingColor) {
                hsb[1] = (float) dragX / colorWidth;
                hsb[2] = 1.0f - (float) dragY / colorWidth;
                setColor(hsb);
            }

            offset += colorWidth + 2;

            Renderer2D.renderSidewaysGradient(context.getMatrices(), getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + 8, Color.BLACK, ColorUtils.getColor(setting.getValue().getColor(), 255));
            Renderer2D.renderOutline(context.getMatrices(), getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + 8, outlineColor);
            hoveringAlpha = isHoveringComponent(mouseX, mouseY, getX() + 1, getY() + offset, getX() + 1 + colorWidth, getY() + offset + 8);
            Renderer2D.renderQuad(context.getMatrices(), getX() + 1 + dragAlpha - 1.5f, getY() + offset - 1, getX() + 1 + dragAlpha + 1.5f, getY() + offset + 9, outlineColor);
            Renderer2D.renderQuad(context.getMatrices(), getX() + 1 + dragAlpha - 0.5f, getY() + offset, getX() + 1 + dragAlpha + 0.5f, getY() + offset + 8, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());

            if (draggingAlpha) {
                setColor(hsb, (int) (255 * (float) dragX / colorWidth));
            }

            offset += 10;
            Renderer2D.renderQuad(context.getMatrices(), getX() + 1, getY() + offset, getX() + (getWidth() / 2f) - 0.5f, getY() + offset + height / 1.2f, DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor());
            hoveringCopy = isHoveringComponent(mouseX, mouseY, getX() + 1, getY() + offset, getX() + (getWidth() / 2f), getY() + offset + height / 1.2f);
            if (hoveringCopy) {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "Copy", getX() + (getWidth() / 4) - 1 - (float) DrugHack.getInstance().getFontManager().getWidth("Copy") / 2, getY() + offset + 2 - 1.2f, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            } else {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "Copy", getX() + (getWidth() / 4) - 1 - (float) DrugHack.getInstance().getFontManager().getWidth("Copy") / 2, getY() + offset + 2, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            }

            Renderer2D.renderQuad(context.getMatrices(), getX() + (getWidth() / 2f) + 0.5f, getY() + offset, getX() + getWidth(), getY() + offset + height / 1.2f, DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor());
            hoveringPaste = isHoveringComponent(mouseX, mouseY, getX() + (getWidth() / 2f) + 0.5f, getY() + offset, getX() + getWidth(), getY() + offset + height / 1.2f);
            if (hoveringPaste) {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "Paste", getX() + (getWidth() / 2) + (getWidth() / 4) - 1 - (float) DrugHack.getInstance().getFontManager().getWidth("Paste") / 2, getY() + offset + 2 - 1.2f, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            } else {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "Paste", getX() + (getWidth() / 2) + (getWidth() / 4) - 1 - (float) DrugHack.getInstance().getFontManager().getWidth("Paste") / 2, getY() + offset + 2, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            }

            offset += height / 1.2f + 1;
            if (setting.getValue().isSync()) Renderer2D.renderQuad(context.getMatrices(), getX() + 1, getY() + offset, getX() + getWidth(), getY() + offset + height / 1.2f, DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor());
            hoveringSync = isHoveringComponent(mouseX, mouseY, getX() + 1, getY() + offset, getX() + getWidth() - 1, getY() + offset + height / 1.2f);
            if (hoveringSync) {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "Sync", getX() + (getWidth() / 2) - (float) DrugHack.getInstance().getFontManager().getWidth("Sync") / 2, getY() + offset + 2 - 1.2f, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            } else {
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "Sync", getX() + (getWidth() / 2) - (float) DrugHack.getInstance().getFontManager().getWidth("Sync") / 2, getY() + offset + 2, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovering(mouseX, mouseY) && mouseButton == 1) {
            open = !open;
            playSound("rclick");
        }

        if (mouseButton == 0) {
            if (hoveringHue) draggingHue = true;
            if (hoveringColor) draggingColor = true;
            if (hoveringAlpha) draggingAlpha = true;

            if (hoveringCopy) {
                DrugHack.getInstance().getClickGuiScreen().setColorClipboard(setting.getValue().getColor());
                playSound("click");
            }

            if (hoveringPaste && DrugHack.getInstance().getClickGuiScreen().getColorClipboard() != null) {
                setting.getValue().setColor(DrugHack.getInstance().getClickGuiScreen().getColorClipboard());
                playSound("click");
            }

            if (hoveringSync) setting.getValue().setSync(!setting.getValue().isSync());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        draggingHue = false;
        draggingColor = false;
        draggingAlpha = false;
    }

    @Override
    public float getHeight() {
        if (open) return 140.5f;
        return 13f;
    }

    private boolean isHoveringComponent(double mouseX, double mouseY, double left, double top, double right, double bottom) {
        return left <= mouseX && top <= mouseY && right > mouseX && bottom > mouseY;
    }

    private void setColor(float[] hsb) {
        setColor(hsb, setting.getValue().getAlpha());
    }

    private void setColor(float[] hsb, int alpha) {
        Color color = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        setting.getValue().setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + height;
    }

    public void updateHSB() {
        Color color = setting.getValue().getColor();
        hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }
}