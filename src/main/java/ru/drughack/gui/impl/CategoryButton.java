package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.render.ColorUtils;

import java.awt.*;

public class CategoryButton extends Button {
    public final Setting<CategorySetting> setting;

    public CategoryButton(Setting<CategorySetting> setting) {
        super(setting.getName(), setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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

        if (isHovering(mouseX, mouseY)) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "...", x + width - 10, y + 4 - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "...", x + width - 10, y + 4, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY) && mouseButton == 1) {
            setting.getValue().setOpen(!setting.getValue().isOpen());
            playSound("rclick");
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public boolean getState() {
        return this.setting.getValue().isOpen();
    }
}