package ru.drughack.utils.other;

import lombok.AllArgsConstructor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import ru.drughack.modules.settings.api.Nameable;

import java.util.HashMap;
import java.util.Map;

public class DrugEvents {

    private static final Map<Identifier, SoundEvent> sounds = new HashMap<>();

    public static final Identifier clickId = Identifier.of("drughack:click");
    public static final SoundEvent clickEvent = SoundEvent.of(clickId);
    public static final Identifier rclickId = Identifier.of("drughack:rclick");
    public static final SoundEvent rclickEvent = SoundEvent.of(rclickId);
    public static final Identifier hoverId = Identifier.of("drughack:hover");
    public static final SoundEvent hoverEvent = SoundEvent.of(hoverId);
    public static final Identifier cssId = Identifier.of("drughack:css");
    public static final SoundEvent cssEvent = SoundEvent.of(cssId);
    public static final Identifier kabanId = Identifier.of("drughack:kaban");
    public static final SoundEvent kabanEvent = SoundEvent.of(kabanId);
    public static final Identifier neverLoseId = Identifier.of("drughack:neverlose");
    public static final SoundEvent neverLoseEvent = SoundEvent.of(neverLoseId);
    public static final Identifier vkId = Identifier.of("drughack:vk");
    public static final SoundEvent vkEvent = SoundEvent.of(vkId);

    @SafeVarargs
    public static void addSounds(Map.Entry<Identifier, SoundEvent>... soundEntries) {
        for (Map.Entry<Identifier, SoundEvent> entry : soundEntries) sounds.put(entry.getKey(), entry.getValue());
    }

    public static void registerSounds() {
        addSounds(
                Map.entry(clickId, clickEvent),
                Map.entry(rclickId, rclickEvent),
                Map.entry(hoverId, hoverEvent),
                Map.entry(cssId, cssEvent),
                Map.entry(kabanId, kabanEvent),
                Map.entry(neverLoseId, neverLoseEvent),
                Map.entry(vkId, vkEvent)
        );

        sounds.forEach((id, event) -> Registry.register(Registries.SOUND_EVENT, id, event));
    }

    @AllArgsConstructor
    public enum Sounds implements Nameable {
        Click("click"),
        RClick("rclick"),
        Hover("hover"),
        Css("css"),
        Kaban("kaban"),
        Neverlose("neverlose"),
        Vk("vk");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}