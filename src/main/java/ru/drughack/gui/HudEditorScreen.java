package ru.drughack.gui;

import lombok.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.gui.api.Frame;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.HudModule;
import ru.drughack.utils.other.CursorUtils;

import java.util.ArrayList;
import java.util.Comparator;

@Getter
public class HudEditorScreen extends Screen {
    private final ArrayList<Frame> frames = new ArrayList<>();
    public HudModule currentDragging;
    public boolean anyHover;

    public HudEditorScreen() {
        super(Text.literal(DrugHack.getInstance().getProtection().getName() + "-hudeditor"));
        frames.add(new Frame(Category.Hud, 50, 70, 110, 18, true));
        for (Frame frame : frames) frame.getButtons().sort(Comparator.comparing(Button::getName));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        anyHover = false;
        for (Frame frame : frames) frame.render(context, mouseX, mouseY, delta);
        if (!anyHover && DrugHack.getInstance().getClickGuiScreen().anyHover) CursorUtils.setCursor(CursorUtils.ARROW);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        for (Frame frame : frames) frame.mouseClicked((int) mouseX, (int) mouseY, clickedButton);
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int releaseButton) {
        for (Frame frame : frames) frame.mouseReleased((int) mouseX, (int) mouseY, releaseButton);
        return super.mouseReleased(mouseX, mouseY, releaseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            for (Frame frame : frames) frame.setY(frame.getY() - 15);
        } else if (verticalAmount > 0) {
            for (Frame frame : frames) frame.setY(frame.getY() + 15);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Frame frame : frames) frame.onKeyPressed(keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Frame frame : frames) frame.onKeyTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}