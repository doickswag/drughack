package ru.drughack.modules.impl.client;

import lombok.AllArgsConstructor;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class Renders extends Module {

    public Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Both);
    public Setting<Mode> mode = new Setting<>("Render Mode", Mode.Shrink);

    public Renders() {
        super("Renders", "Render of client", Category.Client);
        toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (fullNullCheck()) return;
        toggle();
    }

    @AllArgsConstructor
    public enum RenderMode implements Nameable {
        Fill("Fill"),
        Outline("Outline"),
        Both("Both");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Fade("Fade"),
        Shrink("Shrink");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}