package ru.drughack.managers;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.other.DrugEvents;

public class SoundManager implements Wrapper {

    public SoundManager() {
        DrugEvents.registerSounds();
    }

    public void playSound(SoundEvent event, float volume) {
        mc.getSoundManager().play(PositionedSoundInstance.master(event, 1f, volume));
    }
}