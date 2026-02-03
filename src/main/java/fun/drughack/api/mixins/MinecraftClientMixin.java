package fun.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.impl.misc.MultiTask;
import fun.drughack.utils.math.Counter;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        EventTick event = new EventTick();
        DrugHack.getInstance().getEventHandler().post(event);
        Counter.updateFPS();
    }

    @ModifyExpressionValue(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    public boolean handleBlockBreaking(boolean original) {
        if (DrugHack.getInstance().getModuleManager().getModule(MultiTask.class).isToggled()) return false;
        return original;
    }

    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    public boolean doItemUse(boolean original) {
        if (DrugHack.getInstance().getModuleManager().getModule(MultiTask.class).isToggled()) return false;
        return original;
    }

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    public void updateWindowTitle(CallbackInfoReturnable<String> cir) {
        if (!DrugHack.getInstance().isPanic()) cir.setReturnValue("DrugHack 0.1 (Dev)");
    }
}