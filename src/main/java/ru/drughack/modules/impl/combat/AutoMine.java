package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.util.Formatting;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;

public class AutoMine extends Module {

    public Setting<Boolean> onlyCity = new Setting<>("Only City", false);
    public Setting<Boolean> onlyHole = new Setting<>("Only Hole", false);
    public Setting<Boolean> antiCrawl = new Setting<>("Anti Crawl", true);

    public AutoMine() {
        super("AutoMine", "find target and mines auto \n" + Formatting.RED + "requires SpeedMine", Category.Combat);
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Up("Up"),
        Down("Down");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}