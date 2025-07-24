package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.Bind;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class BindButton extends Button {
    private final Setting<Bind> setting;
    public boolean isListening;

    public BindButton(Setting<Bind> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (getState()) Renderer2D.renderQuad(context.getMatrices(), this.x, this.y, this.x + this.width, this.y + this.height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));

        String displayText;
        if (isListening) displayText = "Press a key...";
        else displayText = setting.getName() + " " + setting.getValue().getBind();

        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, displayText, x + 2.3f, y + 2.3f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            CursorUtils.setCursor(CursorUtils.HAND);
            DrugHack.getInstance().getClickGuiScreen().anyHover = true;
            DrugHack.getInstance().getHudEditorScreen().anyHover = true;
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, displayText, x + 2.3f, y + 2.3f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
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
                if (!isListening) playSound("click");
                isListening = true;
            }
        }

        if ((mouseButton == 1 || mouseButton == 2 || mouseButton == 3 || mouseButton == 4) && isListening) {
            setting.setValue(new Bind(mouseButton, false, true));
            isListening = false;
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (isListening) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE) {
                setting.setValue(new Bind(-1, false, false));
            } else {
                setting.setValue(new Bind(key, false, false));
            }
            isListening = false;
        }
    }

    @Override
    public boolean getState() {
        return this.isListening;
    }
}