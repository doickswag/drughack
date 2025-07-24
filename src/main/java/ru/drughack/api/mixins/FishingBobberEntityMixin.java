package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin implements Wrapper {
    @WrapOperation(method = "handleStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;pullHookedEntity(Lnet/minecraft/entity/Entity;)V"))
    private void pushOutOfBlocks(FishingBobberEntity instance, Entity entity, Operation<Void> original) {
        if (entity == mc.player && DrugHack.getInstance().getModuleManager().getVelocity().isToggled() && DrugHack.getInstance().getModuleManager().getVelocity().antiFishingRod.getValue())
            return;

        original.call(instance, entity);
    }
}
