package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements Wrapper {

    @ModifyReturnValue(method = "isPushedByFluids", at = @At("RETURN"))
    private boolean isPushedByFluids(boolean original) {
        if ((Object) this == mc.player && DrugHack.getInstance().getModuleManager().getVelocity().isToggled() && DrugHack.getInstance().getModuleManager().getVelocity().antiLiquidPush.getValue()) return false;
        return original;
    }


    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void getBlockInteractionRange(CallbackInfoReturnable<Double> info) {
        if (DrugHack.getInstance().getModuleManager().getReach().isToggled()) {
            info.setReturnValue(DrugHack.getInstance().getModuleManager().getReach().amount.getValue().doubleValue());
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void getEntityInteractionRange(CallbackInfoReturnable<Double> info) {
        if (DrugHack.getInstance().getModuleManager().getReach().isToggled()) {
            info.setReturnValue(DrugHack.getInstance().getModuleManager().getReach().amount.getValue().doubleValue());
        }
    }
}