package ru.drughack.gui;

import lombok.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.gui.api.Frame;
import ru.drughack.gui.impl.ModuleButton;
import ru.drughack.modules.api.Category;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.other.CursorUtils;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Particle;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

@Getter @Setter
public class ClickGuiScreen extends Screen implements Wrapper {
    private final ArrayList<Frame> frames = new ArrayList<>();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final TimerUtils timer = new TimerUtils();
    public boolean anyHover, searching;
    private Color colorClipboard = null;
    public String currentDescription = "", searchText = "";
    private long openTime;
    public float alpha = 0f;

    public ClickGuiScreen() {
        super(Text.literal(DrugHack.getInstance().getProtection().getName() + "-clickgui"));
        int x = 4;
        for (Category category : DrugHack.getInstance().getModuleManager().getCategories()) {
            if (category == Category.Hud) continue;
            frames.add(new Frame(category, x, 4, 110, 18, true));
            x += 112;
        }
        for (Frame frame : frames) frame.getButtons().sort(Comparator.comparing(Button::getName));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (DrugHack.getInstance().getModuleManager().getUi().blur.getValue()) applyBlur();
        long timeSinceOpen = System.currentTimeMillis() - openTime;
        if (timeSinceOpen < 200) alpha = (float) timeSinceOpen / 200;
        else alpha = 1f;

        anyHover = false;
        if (!searching) searchText = "";
        updateSearch();
        Renderer2D.renderQuad(context.getMatrices(), 0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorUtils.getColor(
                DrugHack.getInstance().getModuleManager().getUi().tint.getValue().getColor(),
                (int) (DrugHack.getInstance().getModuleManager().getUi().tint.getValue().getColor().getAlpha() * alpha)
        ));
        if (DrugHack.getInstance().getModuleManager().getUi().particles.getValue()) {
            if (particles.size() <= 50 && timer.hasTimeElapsed(50)) {
                particles.add(new Particle(
                        (float) MathUtils.random(mc.getWindow().getScaledWidth() - 5, 5),
                        0,
                        20, 20,
                        1, 5,
                        (float) MathUtils.random(360, -360),
                        Identifier.of("drughack", "textures/drughack.png"),
                        ColorUtils.getColor(DrugHack.getInstance().getModuleManager().getUi().color.getValue().getColor(), (int) (255 * alpha))
                ));
                timer.reset();
            }

            Iterator<Particle> iterator = particles.iterator();

            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                particle.animate();
                if (particle.isDead()) iterator.remove();
                else particle.render(context);
            }
        }

        for (Frame frame : frames) frame.render(context, mouseX, mouseY, delta);
        if (!anyHover && DrugHack.getInstance().getHudEditorScreen().anyHover) CursorUtils.setCursor(CursorUtils.ARROW);

        if (!currentDescription.isEmpty() && DrugHack.getInstance().getModuleManager().getUi().description.getValue()) {
            String text = (currentDescription.substring(0, 1).toUpperCase() + currentDescription.substring(1) + ".");
            String[] split = text.split("\n");
            float textWidth = 0f;

            for (String s : split) {
                float tempWidth = DrugHack.getInstance().getFontManager().getWidth(s);
                if (tempWidth > textWidth) textWidth = tempWidth;
            }

            float y = mouseY;
            if (textWidth + (mouseX + 10f) < context.getScaledWindowWidth()) {
                for (String s : split) {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, s, mouseX + 8f, y + 8f, ColorUtils.getColor(
                            Color.WHITE,
                            (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                    ).getRGB());
                    y += DrugHack.getInstance().getFontManager().getHeight() + 1f;
                }
                Renderer2D.renderQuad(context.getMatrices(), mouseX + 7f, mouseY + 7f, mouseX + 8f + textWidth, 8f + y, ColorUtils.getColor(
                        new Color(0, 0, 0, 100),
                        (int) (100 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ));
            } else {
                for (String s : split) {
                    DrugHack.getInstance().getFontManager().drawTextWithShadow(context, s, mouseX - 3f - textWidth, mouseY + 8f, ColorUtils.getColor(
                            Color.WHITE,
                            (int) (255 * DrugHack.getInstance().getClickGuiScreen().alpha)
                    ).getRGB());
                    y += DrugHack.getInstance().getFontManager().getHeight() + 1f;
                }
                Renderer2D.renderQuad(context.getMatrices(), mouseX - 4f - textWidth, mouseY + 7f, mouseX - 3f, 8f + y, ColorUtils.getColor(
                        new Color(0, 0, 0, 100),
                        (int) (100 * DrugHack.getInstance().getClickGuiScreen().alpha)
                ));
            }

            currentDescription = "";
        }

        if (!searching)
            DrugHack.getInstance().getFontManager().drawTextWithShadow(context,
                    "Control + F to search",
                    mc.getWindow().getScaledWidth() - DrugHack.getInstance().getFontManager().getWidth("Control + F to search"),
                    mc.getWindow().getScaledHeight() - DrugHack.getInstance().getFontManager().getHeight() - 1,
                    ColorUtils.getPulse(ColorUtils.getGlobalColor())
            );

        if (searching) {
            float scale = 1.5f;
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, 1f);
            DrugHack.getInstance().getFontManager().drawTextWithShadow(
                    context,
                    searchText + (System.currentTimeMillis() % 1000 > 500 ? "_" : ""),
                    (mc.getWindow().getScaledWidth() / 2f - (DrugHack.getInstance().getFontManager().getWidth(searchText) * scale) / 2f) / scale,
                    (mc.getWindow().getScaledHeight() / 2f) / scale,
                    ColorUtils.getGlobalColor().getRGB()
            );
            context.getMatrices().pop();
        }
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
        if (verticalAmount < 0) for (Frame frame : frames) frame.setY(frame.getY() - 15);
        else if (verticalAmount > 0) for (Frame frame : frames) frame.setY(frame.getY() + 15);

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_F) && !searching) {
            searchText = "";
            searching = true;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE && searching) {
            searchText = "";
            searching = false;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && searching && !searchText.isEmpty()) {
            searchText = searchText.substring(0, searchText.length() - 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER && searching) {
            searching = false;
            return true;
        }

        for (Frame frame : frames) frame.onKeyPressed(keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searching && searchText.length() < 50) searchText += chr;

        for (Frame frame : frames) frame.onKeyTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    private void updateSearch() {
        for (Frame frame : frames)
            for (Button button : frame.getButtons())
                if (button instanceof ModuleButton module)
                    module.setHidden(!module.getName().toLowerCase().contains(searchText.toLowerCase()));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        DrugHack.getInstance().getModuleManager().getUi().toggle();
        super.close();
    }

    @Override
    public void init() {
        super.init();
        this.openTime = System.currentTimeMillis();
    }
}