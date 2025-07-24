package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.drughack.DrugHack;

@Mixin(LimbAnimator.class)
public abstract class LimbAnimatorMixin {

    @ModifyReturnValue(method = "getPos()F", at = @At("RETURN"))
    private float getPos(float original) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().limbSwing.getValue()) {
            return 0;
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "getPos(F)F", at = @At("RETURN"))
    private float getPos2(float original) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().limbSwing.getValue()) {
            return 0;
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "getSpeed()F", at = @At("RETURN"))
    private float getSpeed(float original) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().limbSwing.getValue()) {
            return 0;
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "getSpeed(F)F", at = @At("RETURN"))
    private float getSpeed2(float original) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().limbSwing.getValue()) {
            return 0;
        } else {
            return original;
        }
    }
}