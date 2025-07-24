package ru.drughack.modules.impl.client;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventChangeSetting;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.builders.*;
import ru.drughack.utils.fonts.FontRenderer;

import java.awt.Font;

public class FontModule extends Module {

    public Setting<Boolean> customFont = BooleanSetting.builder("CustomFont")
            .defaultValue(false)
            .description("makes font custom")
            .build().toSetting();

    public Setting<String> name = StringSetting.builder("Name")
            .defaultValue("Verdana")
            .description("select of font")
            .build().toSetting();

    public Setting<Integer> size = IntSetting.builder("Size")
            .defaultValue(16)
            .min(8)
            .max(48)
            .description("size of font")
            .build().toSetting();

    public Setting<Style> style = EnumSetting.builder("Style", Style.Plain)
            .description("style of font")
            .build().toSetting();

    public Setting<Boolean> global = BooleanSetting.builder("Global")
            .description("makes font global for all minecraft")
            .defaultValue(false)
            .build().toSetting();

    public Setting<Integer> xOffset = IntSetting.builder("XOffset")
            .defaultValue(0)
            .min(-10)
            .max(10)
            .description("xoffset of font")
            .build().toSetting();

    public Setting<Integer> yOffset = IntSetting.builder("YOffset")
            .defaultValue(-2)
            .min(-10)
            .max(10)
            .description("yoffset of font")
            .build().toSetting();

    public Setting<Integer> widthOffset = IntSetting.builder("WidthOffset")
            .defaultValue(0)
            .min(-10)
            .max(10)
            .description("widthoffset of font")
            .build().toSetting();

    public Setting<Integer> heightOffset = IntSetting.builder("HeightOffset")
            .defaultValue(0)
            .min(-10)
            .max(10)
            .description("heightoffset of font")
            .build().toSetting();

    public Setting<Shadow> shadowMode = EnumSetting.builder("ShadowMode", Shadow.Default)
            .description("mode of shadow")
            .build().toSetting();

    public Setting<Float> shadowOffset = FloatSetting.builder("ShadowOffset")
            .defaultValue(0.5f)
            .min(-2.0f)
            .max(2.0f)
            .description("shadowoffset of font")
            .build().toSetting();

    public FontModule() {
        super("Font", "font of the client", Category.Client);
        toggle();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        updateFontRenderer();
    }

    @EventHandler
    public void onChangeSetting(EventChangeSetting e) {
        if (fullNullCheck()) return;
        if (e.getSetting() == customFont || e.getSetting() == name || e.getSetting() == size) updateFontRenderer();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (fullNullCheck()) return;
        toggle();
        updateFontRenderer();
    }

    private void updateFontRenderer() {
        if (fullNullCheck()) return;
        DrugHack.getInstance().getFontManager().setFontRenderer(new FontRenderer(new Font[]{new Font(name.getValue(), style.getValue() == Style.BoldItalic ? Font.BOLD | Font.ITALIC : style.getValue() == Style.Bold ? Font.BOLD : style.getValue() == Style.Italic ? Font.ITALIC : Font.PLAIN, size.getValue())}, size.getValue().floatValue() / 2.0f));
    }

    @AllArgsConstructor
    public enum Style implements Nameable {
        Plain("Plain"),
        Bold("Bold"),
        Italic("Italic"),
        BoldItalic("BoldItalic");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Shadow implements Nameable {
        None("None"),
        Custom("Custom"),
        Default("Default");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}