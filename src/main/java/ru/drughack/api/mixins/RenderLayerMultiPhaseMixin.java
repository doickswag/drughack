package ru.drughack.api.mixins;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.drughack.utils.mixins.IMultiPhase;

@Mixin(RenderLayer.MultiPhase.class)
public abstract class RenderLayerMultiPhaseMixin implements IMultiPhase {
    @Shadow
    @Final
    private RenderLayer.MultiPhaseParameters phases;

    @Override
    public RenderLayer.MultiPhaseParameters drughack$getParameters() {
        return this.phases;
    }
}
