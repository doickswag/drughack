package ru.drughack.api.mixins;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventKey;
import ru.drughack.utils.interfaces.Wrapper;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin implements Wrapper {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 0) DrugHack.getInstance().getModuleManager().onKeyReleased(key);
        if (action == 1) DrugHack.getInstance().getModuleManager().onKeyPressed(key);
        DrugHack.getInstance().getEventHandler().post(new EventKey(key, action));
    }
}