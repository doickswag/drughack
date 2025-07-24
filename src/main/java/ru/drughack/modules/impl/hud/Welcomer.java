package ru.drughack.modules.impl.hud;

import lombok.AllArgsConstructor;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.render.ColorUtils;

public class Welcomer extends HudModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Uid);
    private final Setting<String> text = new Setting<>("Text", "Welcome to drughack.cc!", v -> mode.getValue() == Mode.Custom);

    public Welcomer() {
        super("Welcomer", -1, -1);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);

        String finalText = mode.getValue() == Mode.Custom ? text.getValue() : "Hello %s! Have fun with drughack.cc".formatted(mode.getValue() == Mode.Uid ? Formatting.WHITE + "uid" + DrugHack.getInstance().getProtection().getUid() + Formatting.RESET : Formatting.WHITE + DrugHack.getInstance().getProtection().getUser() + Formatting.RESET);
        DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), finalText, (int) getX(), (int) getY(), ColorUtils.getGlobalColor());
        setBounds(getX(), getY(), DrugHack.getInstance().getFontManager().getWidth(finalText), DrugHack.getInstance().getFontManager().getHeight());
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Uid("Uid"),
        User("User"),
        Custom("Custom");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}