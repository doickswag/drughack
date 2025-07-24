package ru.drughack.modules.api;

import lombok.Getter;
import lombok.Setter;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventMouse;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.gui.HudEditorScreen;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.PosSetting;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

@Getter @Setter
public class HudModule extends Module {
    public final Setting<PosSetting> pos = new Setting<>("Position", new PosSetting(0.5f, 0.5f));
    private float dragX, dragY, width, height;
    private boolean dragging, button;

    public HudModule(String name, int width, int height) {
        super(name, "", Category.Hud);
        this.width = width;
        this.height = height;
    }

    public float getX() {
        return mc.getWindow().getScaledWidth() * pos.getValue().getX();
    }

    public float getY() {
        return mc.getWindow().getScaledHeight() * pos.getValue().getY();
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (!(mc.currentScreen instanceof HudEditorScreen) || fullNullCheck()) return;

        float x = getX();
        float y = getY();

        if (button) {
            if (!dragging && isHovering() && !DrugHack.getInstance().getHudEditorScreen().anyHover) {
                dragX = gavno$X() - x;
                dragY = gavno$Y() - y;
                dragging = true;
                DrugHack.getInstance().getHudEditorScreen().currentDragging = this;
            }

            if (dragging) {
                float finalX = Math.min(Math.max(gavno$X() - dragX, 0), mc.getWindow().getScaledWidth() - width);
                float finalY = Math.min(Math.max(gavno$Y() - dragY, 0), mc.getWindow().getScaledHeight() - height);

                pos.getValue().setX(finalX / mc.getWindow().getScaledWidth());
                pos.getValue().setY(finalY / mc.getWindow().getScaledHeight());
            }
        } else dragging = false;

        if (isHovering()) {
            int textWidth = DrugHack.getInstance().getFontManager().getWidth(getName());
            int textHeight = DrugHack.getInstance().getFontManager().getHeight();
            if (x + width + 5 + textWidth > mc.getWindow().getScaledWidth()) DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), getName(), x - 5 - textWidth, y + height / 2f - textHeight / 2f, -1);
            else DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), getName(), x + width + 5, y + height / 2f - textHeight / 2f, -1);
        }

        float left = Math.min(x, x + width);
        float top = Math.min(y, y + height);
        float right = Math.max(x, x + width);
        float bottom = Math.max(y, y + height);
        Renderer2D.renderOutline(e.getContext().getMatrices(), left - 1, top - 1, right + 1, bottom + 1, DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor());
    }

    @Override
    public void onMouse(EventMouse e) {
        if (!(mc.currentScreen instanceof HudEditorScreen) || fullNullCheck()) return;

        if (e.getAction() == 0) {
            button = false;
            dragging = false;
            DrugHack.getInstance().getHudEditorScreen().currentDragging = null;
        } else if (e.getAction() == 1 && isHovering() && DrugHack.getInstance().getHudEditorScreen().currentDragging == null) button = true;
    }

    public int gavno$X() {
        return (int) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    public int gavno$Y() {
        return (int) (mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }

    public void setBounds(float x, float y, float width, float height) {
        this.width = width;
        this.height = height;
        pos.getValue().setX(x / mc.getWindow().getScaledWidth());
        pos.getValue().setY(y / mc.getWindow().getScaledHeight());
    }

    public boolean isHovering() {
        float x = getX();
        float y = getY();
        float left = Math.min(x, x + width);
        float right = Math.max(x, x + width);
        float top = Math.min(y, y + height);
        float bottom = Math.max(y, y + height);

        return gavno$X() >= left - 1 && gavno$X() <= right + 1 && gavno$Y() >= top - 1 && gavno$Y() <= bottom + 1;
    }
}