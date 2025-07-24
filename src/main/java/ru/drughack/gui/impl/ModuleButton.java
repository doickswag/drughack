package ru.drughack.gui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.drughack.DrugHack;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.impl.Bind;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.modules.settings.Setting;
import ru.drughack.gui.api.Button;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Button implements Wrapper {
    private final Module module;
    public final List<Button> buttons = new ArrayList<>();
    public boolean open, hover, hover2;
    private float animatedWidth = 0;
    private long lastAnimationTime = 0;

    public ModuleButton(Module module) {
        super(module.getName(), module.getDescription());
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting.isBooleanSetting()) {
                buttons.add(new BooleanButton((Setting<Boolean>) setting));
            } else if (setting.isBindSetting() && setting != module.bind) {
                buttons.add(new BindButton((Setting<Bind>) setting));
            } else if ((setting.isStringSetting() || setting.isCharacterSetting())) {
                buttons.add(new StringButton((Setting<String>) setting));
            } else if (setting.isNumberSetting() && setting.isNumber()) {
                buttons.add(new SliderButton((Setting<Number>) setting));
            } else if (setting.isNameSetting()) {
                buttons.add(new NameButton((Setting<Enum<?>>) setting));
            } else if (setting.isColorSetting()) {
                buttons.add(new ColorButton((Setting<ColorSetting>) setting));
            } else if (setting.isCategorySetting()) {
                buttons.add(new CategoryButton((Setting<CategorySetting>) setting));
            } else if (setting.isCategoryBooleanSetting()) {
                buttons.add(new CategoryBooleanButton((Setting<CategoryBooleanSetting>) setting));
            }
        }

        if (!(module instanceof HudModule)) buttons.add(new BindExtraButton((Setting<Bind>) module.getSettingByName("Key")));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hover = isHovering(mouseX, mouseY);
        int textColor = getState() ? ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().enabledColor.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().enabledColor.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ).getRGB() : ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().textColor.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().textColor.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ).getRGB();
        if (hover) {
            if (!hover2) playSound("hover");
            DrugHack.getInstance().getClickGuiScreen().setCurrentDescription(getDescription());
        }

        hover2 = hover;

        updateAnimation();

        Renderer2D.renderQuad(context.getMatrices(), x, y, x + this.animatedWidth, y + height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().bgEnabled.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().bgEnabled.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));
        Renderer2D.renderQuad(context.getMatrices(), x, y, x + this.width, y + height, ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().bgButton.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().bgButton.getValue().getColor().getAlpha() * DrugHack.getInstance().getClickGuiScreen().alpha)
        ));

        if (hover) {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, module.getName(), x + 2.3f, y + 2.3f - 1.2f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, module.getName(), x + 2.3f, y + 2.3f - 1.2f, textColor);
            CursorUtils.setCursor(CursorUtils.HAND);
            DrugHack.getInstance().getClickGuiScreen().anyHover = true;
            DrugHack.getInstance().getHudEditorScreen().anyHover = true;
        } else {
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, module.getName(), x + 2.3f, y + 2.3f, ColorUtils.getColor(
                    Color.WHITE,
                    (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
            ).getRGB());
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context, module.getName(), x + 2.3f, y + 2.3f, textColor);
        }

        if (DrugHack.getInstance().getModuleManager().getUi().gear.getValue() && !buttons.isEmpty()) {
            if (!open) {
                if (hover) {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "+", x + width - 10, y + 2.3f - 1.2f, ColorUtils.getColor(
                            Color.WHITE,
                            (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                    ).getRGB());
                } else {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "+", x + width - 10, y + 2.3f, ColorUtils.getColor(
                            Color.WHITE,
                            (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                    ).getRGB());
                }
            } else {
                if (hover) {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "-", x + width - 10, y + 2.3f - 1.2f, ColorUtils.getColor(
                            Color.WHITE,
                            (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                    ).getRGB());
                } else {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "-", x + width - 10, y + 2.3f, ColorUtils.getColor(
                            Color.WHITE,
                            (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                    ).getRGB());
                }
            }
        }

        if (DrugHack.getInstance().getModuleManager().getUi().bind.getValue()) renderBind(context);

        if (open) {
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.render(context, mouseX, mouseY, delta);
                if (button.isHovering(mouseX, mouseY)) DrugHack.getInstance().getClickGuiScreen().setCurrentDescription(button.getDescription());
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

        if (isHovering(mouseX, mouseY)) {
            if (mouseButton == 0) {
                module.toggle();
                playSound("click");
            } else if (mouseButton == 1) {
                open = !open;
                playSound("rclick");
            }
        }

        if (open) {
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (open) {
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onKeyPressed(int key) {
        super.onKeyPressed(key);
        if (open) {
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.onKeyPressed(key);
            }
        }
    }

    @Override
    public boolean getState() {
        return module.isToggled();
    }

    private void renderBind(DrawContext context) {
        if (module != null) {
            if (!module.getBind().isEmpty()) {
                String bind = module.getBind().getBind();
                float scale = 0.5f;
                float nameWidth = DrugHack.getInstance().getFontManager().getWidth(module.getName());
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.scale(scale, scale, scale);
                DrugHack.getInstance().getFontManager().drawTextWithShadow(context, "[" + bind + "]", (x + nameWidth + 3) / scale, (y + 2) / scale, ColorUtils.getColor(
                        Color.WHITE,
                        (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ).getRGB());
                matrices.pop();
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        super.mouseReleased(mouseX, mouseY, releaseButton);
        if (open) {
            for (Button button : buttons) {
                if (button.isHidden()) continue;
                button.mouseReleased(mouseX, mouseY, releaseButton);
            }
        }
    }
}