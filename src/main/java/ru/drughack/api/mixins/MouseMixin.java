package ru.drughack.api.mixins;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventMouse;
import ru.drughack.api.event.impl.EventPlayerChangeLook;

import static ru.drughack.utils.interfaces.Wrapper.mc;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            if (action == 0) DrugHack.getInstance().getModuleManager().onMouseKeyReleased(button);
            if (action == 1) DrugHack.getInstance().getModuleManager().onMouseKeyPressed(button);
            DrugHack.getInstance().getEventHandler().post(new EventMouse(button, action));
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (DrugHack.getInstance().getModuleManager().getZoom().isToggled()) {
            DrugHack.getInstance().getModuleManager().getZoom().mouseScroll((float) vertical);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;isCursorLocked()Z", shift = At.Shift.BEFORE))
    public void tick(CallbackInfo ci) {
        DrugHack.getInstance().getEventHandler().post(new EventPlayerChangeLook.Pre());
    }
}