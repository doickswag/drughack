package ru.drughack.gui.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import ru.drughack.DrugHack;
import ru.drughack.gui.impl.ModuleButton;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.other.DrugEvents;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Frame {
    public final List<Button> buttons = new ArrayList<>();
    public float x, y, dragX, dragY, width, height, totalHeight;
    public boolean open, dragging, hidden = false;
    public final Category category;
    private float animatedHeight = 0;
    private long lastAnimationTime = 0;

    public Frame(Category category, float x, float y, float width, float height, boolean open) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = open;
        this.animatedHeight = open ? getButtonHeight() - 2f : -5f;
        for (Module module : DrugHack.getInstance().getModuleManager().getModules(category)) buttons.add(new ModuleButton(module));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            this.x = this.dragX + mouseX;
            this.y = this.dragY + mouseY;
        }

        updateAnimation();

        Renderer2D.renderQuad(context.getMatrices(), x, y - 1, x + width, y + height + animatedHeight, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().bgColor.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().bgColor.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));
        if (DrugHack.getInstance().getModuleManager().getUi().line.getValue()) Renderer2D.renderOutline(context.getMatrices(), x, y + 12, x + width, (y + height) + animatedHeight, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));
        Renderer2D.renderQuad(context.getMatrices(), x, y - 1, x + width, y + height - 6, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));
        DrugHack.getInstance().getFontManager().drawTextWithShadow(context, category.name(), x + 3.0f, y - 3f + 4.5f, ColorUtils.getColor(
                Color.WHITE,
                (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
        ).getRGB());

        this.totalHeight = height - 3f;

        if (animatedHeight > 0f) {
            context.enableScissor((int) x, (int) (y + height - 6), (int) (x + width), (int) (y + height + animatedHeight));

            for (Button button : buttons) {
                button.update();
                if (!button.isHidden()) {
                    button.setX(x + 2.5f);
                    button.setY(y + totalHeight);
                    button.setWidth(getWidth() - 5f);
                    button.render(context, mouseX, mouseY, partialTicks);
                    totalHeight += button.getHeight() + 1.5f;
                }

                if (button instanceof ModuleButton mb) {
                    for (Button b : mb.buttons) {
                        b.update();
                        if (!b.isHidden() && mb.open) {
                            b.setX(x + 4f);
                            b.setY(y + totalHeight);
                            b.setWidth(button.getWidth() - 1.5f);
                            b.render(context, mouseX, mouseY, partialTicks);
                            totalHeight += b.getHeight() + 1.5f;
                        }
                    }
                }
            }

            context.disableScissor();
        }
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (this.lastAnimationTime == 0) this.lastAnimationTime = currentTime;
        float deltaTime = (currentTime - this.lastAnimationTime) / 1000.0f;
        this.lastAnimationTime = currentTime;
        float targetHeight = this.open ? getButtonHeight() - 2f : -5f;
        this.animatedHeight = MathUtils.lerp$(this.animatedHeight, targetHeight, deltaTime * 8.0f);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            dragX = x - mouseX;
            dragY = y - mouseY;
            for (Frame frame : DrugHack.getInstance().getClickGuiScreen().getFrames()) if (frame.dragging) frame.dragging = false;
            dragging = true;
            return;
        }

        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            open = !open;
            DrugHack.getInstance().getSoundManager().playSound(DrugEvents.rclickEvent, DrugHack.getInstance().getModuleManager().getUi().volume.getValue());
            return;
        }

        if (open || animatedHeight > 0f){
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) dragging = false;
        if (open || animatedHeight > 0f){
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.mouseReleased(mouseX, mouseY, releaseButton);
            }
        }
    }

    public void onKeyTyped(char typedChar, int keyCode) {
        if (open || animatedHeight > 0f){
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    public void onKeyPressed(int key) {
        if (open || animatedHeight > 0f){
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.onKeyPressed(key);
            }
        }
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight() - (open ? 6f : 0f);
    }

    private float getButtonHeight() {
        float height = 0f;
        for (Button button : buttons) {
            if (button.isHidden()) continue;
            height += button.getHeight() + 1.5f;
            if (button instanceof ModuleButton mb && mb.open) for (Button b : mb.buttons) if (!b.isHidden()) height += b.getHeight() + 1.5f;
        }
        return height;
    }
}