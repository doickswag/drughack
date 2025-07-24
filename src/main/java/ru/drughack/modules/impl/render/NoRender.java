package ru.drughack.modules.impl.render;

import lombok.AllArgsConstructor;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class NoRender extends Module {

    public Setting<Boolean> hurtCamera = new Setting<>("HurtCamera", true);
    public Setting<Boolean> explosions = new Setting<>("Explosions", true);
    public Setting<Boolean> fireOverlay = new Setting<>("FireOverlay", true);
    public Setting<Boolean> blockOverlay = new Setting<>("BlockOverlay", false);
    public Setting<Boolean> liquidOverlay = new Setting<>("LiquidOverlay", false);
    public Setting<Boolean> snowOverlay = new Setting<>("SnowOverlay", false);
    public Setting<Boolean> pumpkinOverlay = new Setting<>("PumpkinOverlay", true);
    public Setting<Boolean> portalOverlay = new Setting<>("PortalOverlay", false);
    public Setting<Boolean> totemAnimation = new Setting<>("TotemAnimation", false);
    public Setting<Boolean> bossBar = new Setting<>("BossBar", false);
    public Setting<Boolean> vignette = new Setting<>("Vignette", true);
    public Setting<Boolean> blindness = new Setting<>("Blindness", true);
    public Setting<Boolean> fog = new Setting<>("Fog", false);
    public Setting<Boolean> textShadow = new Setting<>("TextShadow", false);
    public Setting<Boolean> signText = new Setting<>("SignText", false);
    public Setting<Boolean> armor = new Setting<>("Armor", false);
    public Setting<Boolean> limbSwing = new Setting<>("LimbSwing", false);
    public Setting<Boolean> corpses = new Setting<>("Corpses", false);
    public Setting<Entities> tileEntities = new Setting<>("TileEntities", Entities.Never);
    public Setting<Integer> tileDistance = new Setting<>("TileDistance", 10, 0, 25, v -> tileEntities.getValue() == Entities.Distance);

    public NoRender() {
        super("NoRender", "no render", Category.Render);
    }

    @AllArgsConstructor
    public enum Entities implements Nameable {
        Never("Never"),
        Distance("Distance"),
        Always("Always");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}