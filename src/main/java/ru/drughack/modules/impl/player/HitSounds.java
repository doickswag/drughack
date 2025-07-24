package ru.drughack.modules.impl.player;

import meteordevelopment.orbit.EventHandler;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventAttackEntity;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.other.DrugEvents;

public class HitSounds extends Module {

    private final Setting<DrugEvents.Sounds> sound = new Setting<>("Sound", DrugEvents.Sounds.Css);
    private final Setting<Float> volume = new Setting<>("Volume", 0.8f, 0f, 1f);

    public HitSounds() {
        super("HitSounds", "sounds after hit", Category.Player);
    }

    @EventHandler
    public void onAttack(EventAttackEntity e) {
        switch (sound.getValue()) {
            case DrugEvents.Sounds.Click -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.clickEvent, volume.getValue());
            case DrugEvents.Sounds.RClick -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.rclickEvent, volume.getValue());
            case DrugEvents.Sounds.Hover -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.hoverEvent, volume.getValue());
            case DrugEvents.Sounds.Css -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.cssEvent, volume.getValue());
            case DrugEvents.Sounds.Kaban -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.kabanEvent, volume.getValue());
            case DrugEvents.Sounds.Neverlose -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.neverLoseEvent, volume.getValue());
            case DrugEvents.Sounds.Vk -> DrugHack.getInstance().getSoundManager().playSound(DrugEvents.vkEvent, volume.getValue());
        }
    }
}