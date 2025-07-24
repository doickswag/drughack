package ru.drughack.api.mixins;

import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.modules.impl.render.Fullbright;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Shadow @Final private SimpleFramebuffer lightmapFramebuffer;

    @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
    private void getDarknessFactor(float delta, CallbackInfoReturnable<Float> info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().blindness.getValue()) {
            info.setReturnValue(0.0f);
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V", shift = At.Shift.BEFORE))
    private void update$endWrite(float delta, CallbackInfo info) {
        Fullbright module = DrugHack.getInstance().getModuleManager().getFullbright();
        if (module.isToggled() && module.mode.getValue() == Fullbright.Mode.Gamma) {
            lightmapFramebuffer.clear();
        }
    }
}