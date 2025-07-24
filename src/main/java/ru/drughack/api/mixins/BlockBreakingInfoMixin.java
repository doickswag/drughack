package ru.drughack.api.mixins;

import net.minecraft.entity.player.BlockBreakingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventPlayerMine;

@Mixin(BlockBreakingInfo.class)
public abstract class BlockBreakingInfoMixin {
    @Inject(method = "compareTo(Lnet/minecraft/entity/player/BlockBreakingInfo;)I", at = @At("HEAD"))
    private void compareTo(BlockBreakingInfo blockBreakingInfo, CallbackInfoReturnable<Integer> cir) {
        DrugHack.getInstance().getEventHandler().post(new EventPlayerMine(blockBreakingInfo.getActorId(), blockBreakingInfo.getPos()));
    }
}