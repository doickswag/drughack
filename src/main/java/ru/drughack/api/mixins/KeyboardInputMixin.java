package ru.drughack.api.mixins;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventKeyboardTick;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tick$TAIL(CallbackInfo info) {
        EventKeyboardTick event = new EventKeyboardTick(movementForward, movementSideways);
        DrugHack.getInstance().getEventHandler().post(event);

        if (event.isCanceled()) {
            this.movementForward = event.getMovementForward();
            this.movementSideways = event.getMovementSideways();
        }
    }
}