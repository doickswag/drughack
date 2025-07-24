package ru.drughack.modules.impl.client;

import org.lwjgl.glfw.GLFW;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.gui.ClickGuiScreen;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.api.Category;
import ru.drughack.utils.other.CursorUtils;

import java.awt.*;

public class UI extends Module {

    public Setting<Float> volume = new Setting<>("Volume", 0.8f, 0f, 1f);
    public Setting<String> prefix = new Setting<>("Prefix", ".");
    public Setting<CategorySetting> text = new Setting<>("Text", new CategorySetting());

    public Setting<ColorSetting> textColor = new Setting<>("TextColor",
            new ColorSetting(new ColorSetting.Colors(new Color(249, 244, 255), false)),
            v -> text.getValue().isOpen()
    );

    public Setting<ColorSetting> enabledColor = new Setting<>("EnabledColor",
            new ColorSetting(new ColorSetting.Colors(new Color(221, 201, 255), false)),
            v -> text.getValue().isOpen()
    );

    public Setting<CategorySetting> colors = new Setting<>("Colors", new CategorySetting());

    public Setting<ColorSetting> color = new Setting<>("Color",
            new ColorSetting(new ColorSetting.Colors(new Color(153, 153, 255, 115), false)),
            v -> colors.getValue().isOpen()
    );

    public Setting<ColorSetting> bgColor = new Setting<>("BgColor",
            new ColorSetting(new ColorSetting.Colors(new Color(2, 0, 8, 119), false)),
            v -> colors.getValue().isOpen()
    );

    public Setting<ColorSetting> bgButton = new Setting<>("BgButton",
            new ColorSetting(new ColorSetting.Colors(new Color(130, 128, 219, 42), false)),
            v -> colors.getValue().isOpen()
    );

    public Setting<ColorSetting> bgEnabled = new Setting<>("BgEnabled",
            new ColorSetting(new ColorSetting.Colors(new Color(153, 153, 255, 115), false)),
            v -> colors.getValue().isOpen()
    );

    public Setting<ColorSetting> tint = new Setting<>("Tint",
            new ColorSetting(new ColorSetting.Colors(new Color(255, 0, 117, 37), false)),
            v -> colors.getValue().isOpen()
    );

    public Setting<CategorySetting> elements = new Setting<>("Elements", new CategorySetting());
    public Setting<Boolean> bind = new Setting<>("Binds", true, v -> elements.getValue().isOpen());
    public Setting<Boolean> description = new Setting<>("Description",  true, v -> elements.getValue().isOpen());
    public Setting<Boolean> gear = new Setting<>("Gear", true, v -> elements.getValue().isOpen());
    public Setting<Boolean> line = new Setting<>("Line", true, v -> elements.getValue().isOpen());
    public Setting<Boolean> blur = new Setting<>("Blur", true, v -> elements.getValue().isOpen());
    public Setting<Boolean> particles = new Setting<>("Particles", true, v -> elements.getValue().isOpen());

    public UI() {
        super("UI", "clickgui of client", Category.Client);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT, false, false);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (fullNullCheck()) return;
        mc.setScreen(DrugHack.getInstance().getClickGuiScreen());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (fullNullCheck()) return;
        mc.setScreen(null);
        CursorUtils.setCursor(CursorUtils.ARROW);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;
        if (!(mc.currentScreen instanceof ClickGuiScreen)) toggle();
    }
}