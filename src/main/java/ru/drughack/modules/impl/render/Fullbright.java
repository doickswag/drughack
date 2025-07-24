package ru.drughack.modules.impl.render;

import lombok.AllArgsConstructor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;

public class Fullbright extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Gamma);

    public Fullbright() {
        super("Fullbright", "bright but full", Category.Render);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (!(mode.getValue() == Mode.Potion)) return;
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE));
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;
        if (!(mode.getValue() == Mode.Potion)) return;
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (!(mode.getValue() == Mode.Potion)) return;

        if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Gamma("Gamma"),
        Potion("Potion");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}