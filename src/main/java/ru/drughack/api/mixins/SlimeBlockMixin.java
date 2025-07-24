package ru.drughack.api.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin implements Wrapper {

    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo info) {
        if (entity == mc.player && DrugHack.getInstance().getModuleManager().getNoSlow().isToggled() && DrugHack.getInstance().getModuleManager().getNoSlow().slimeBlocks.getValue()) {
            info.cancel();
        }
    }
}