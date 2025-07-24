package ru.drughack.api.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getSlipperiness", at = @At("HEAD"), cancellable = true)
    private void getSlipperiness(CallbackInfoReturnable<Float> info) {
        if ((Object) this == Blocks.SLIME_BLOCK && DrugHack.getInstance().getModuleManager().getNoSlow().isToggled() && DrugHack.getInstance().getModuleManager().getNoSlow().slimeBlocks.getValue()) {
            info.setReturnValue(0.6f);
        }
    }
}