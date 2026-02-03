package fun.drughack.screen.clickgui;

import fun.drughack.DrugHack;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.screen.clickgui.components.impl.ModuleComponent;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Panel implements Wrapper {
    @Setter private float x, y, width, height;
    private final Category category;
    @Getter private final List<ModuleComponent> components = new ArrayList<>();
    private final Animation openAnimation = new Animation(250, 1f, true, Easing.BOTH_SINE);
    private final InfinityAnimation scrollAnimation = new InfinityAnimation(Easing.LINEAR);
    private float scroll;
    private boolean open = true;

    public Panel(float x, float y, float width, float height, Category category) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        for (Module module : DrugHack.getInstance().getModuleManager().getModules(category)) components.add(new ModuleComponent(module));
        components.sort(Comparator.comparing(ModuleComponent::getName));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnimation.update(open);
        scroll = MathHelper.clamp(scroll, 0f, Math.max(0, getTotalHeight() - 230f));
        Render2D.drawStyledRect(context.getMatrices(), x, y, width, (250f * openAnimation.getValue()) + (height * openAnimation.getReversedValue()), 5f, new Color(0, 0, 0, 220), 255);
        Render2D.drawFont(context.getMatrices(), Fonts.SEMIBOLD.getFont(9f), category.name(), x + 6f, y + 5f, Color.WHITE);
        Render2D.drawFont(context.getMatrices(), Fonts.ICONS.getFont(10f), category.getIcon(), x + width - 15f, y + 5f, Color.WHITE);

        if (openAnimation.getValue() > 0) Render2D.drawStyledRect(context.getMatrices(), x, y + height - 1, width, 1, 1f, new Color(255, 255, 255, 25), 255);

        Render2D.startScissor(
                context,
                x,
                y + height,
                width,
                230 * openAnimation.getValue()
        );

        float currentY = y + height - scrollAnimation.animate(scroll, 150);
        if (openAnimation.getValue() > 0f) {
            for (ModuleComponent component : components) {
                component.setX(x);
                component.setY(currentY);
                component.setWidth(width);
                component.setHeight(height);
                component.render(context, mouseX, mouseY, delta);
                currentY += component.getHeight();

                if (component.getOpenAnimation().getValue() > 0f) {
                    float currentYV2 = currentY;
                    float maxHeight = 0f;

                    for (Component subComponent : component.getComponents()) {
                        if (!subComponent.getVisible().get()) continue;
                        maxHeight += (subComponent.getHeight() + subComponent.getAddHeight().get());
                    }

                    Render2D.startScissor(context, x, currentYV2, width, maxHeight * component.getOpenAnimation().getValue());

                    for (Component sub : component.getComponents()) {
                        if (!sub.getVisible().get()) continue;
                        sub.setX(x + 2f);
                        sub.setY(currentY);
                        sub.setWidth(width - 4f);
                        sub.setHeight(height - 5f);
                        Render2D.startScissor(context, sub.getX(), sub.getY(), sub.getWidth(), sub.getHeight() + sub.getAddHeight().get());
                        sub.render(context, mouseX, mouseY, delta);
                        Render2D.stopScissor(context);
                        currentY += (sub.getHeight() + sub.getAddHeight().get());
                    }

                    Render2D.stopScissor(context);
                    currentY = currentYV2 + (maxHeight * component.getOpenAnimation().getValue());
                }
            }
        }

        Render2D.stopScissor(context);
    }

    private float getTotalHeight() {
        float totalHeight = 0f;
        for (ModuleComponent component : components) {
            totalHeight += component.getHeight();
            if (component.getOpenAnimation().getValue() > 0f) {
                for (Component sub : component.getComponents()) {
                    if (!sub.getVisible().get()) continue;
                    totalHeight += (sub.getHeight() + sub.getAddHeight().get()) * component.getOpenAnimation().getValue();
                }
            }
        }

        return totalHeight;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtils.isHovered(x, y, width, height, (float) mouseX, (float) mouseY) && button == 1) {
            open = !open;
            return;
        }

        if (open && MathUtils.isHovered(x, y + height, width, 230f * openAnimation.getValue(), (float) mouseX, (float) mouseY)) for (ModuleComponent component : components) component.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (open) for (ModuleComponent component : components) component.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (open && MathUtils.isHovered(x, y + height, width, 230f * openAnimation.getValue(), (float) mouseX, (float) mouseY)) scroll -= (float) (verticalAmount * 20);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (open) for (ModuleComponent component : components) component.keyPressed(keyCode, scanCode, modifiers);
    }

    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        if (open) for (ModuleComponent component : components) component.keyReleased(keyCode, scanCode, modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        if (open) for (ModuleComponent component : components) component.charTyped(chr, modifiers);
    }
}