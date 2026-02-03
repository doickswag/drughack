package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.modules.impl.render.NoRender;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V", ordinal = 0))
    public void update2(Args args) {
        if (DrugHack.getInstance().getModuleManager().getModule(NoRender.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoRender.class).clip.getValue()) args.set(0, -3.5f);
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    public void clipToSpace(float f, CallbackInfoReturnable<Float> cir) {
        if (DrugHack.getInstance().getModuleManager().getModule(NoRender.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoRender.class).clip.getValue()) cir.setReturnValue(3.5f);
    }
}