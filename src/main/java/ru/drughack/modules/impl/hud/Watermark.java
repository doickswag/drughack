package ru.drughack.modules.impl.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.render.ColorUtils;

public class Watermark extends HudModule {
    private final Setting<String> text = new Setting<>("Text", DrugHack.getInstance().getProtection().getName());

    public Watermark() {
        super("Watermark", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);

        DrawContext context = e.getContext();
        String watermarkText = text.getValue()
                + " "
                + Formatting.WHITE + DrugHack.getInstance().getProtection().getVersion()
                + " "
                + Formatting.GRAY
                + DrugHack.getInstance().getProtection().getBuildTime();

        DrugHack.getInstance().getFontManager().drawTextWithShadow(context, watermarkText, (int) getX(), (int) getY(), ColorUtils.getGlobalColor());
        setBounds(getX(), getY(), DrugHack.getInstance().getFontManager().getWidth(watermarkText), DrugHack.getInstance().getFontManager().getHeight());
    }
}