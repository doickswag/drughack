package ru.drughack.api.mixins;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.drughack.utils.mixins.IMultiPhaseParameters;

@Mixin(RenderLayer.MultiPhaseParameters.class)
public abstract class RenderLayerMultiPhaseParametersMixin implements IMultiPhaseParameters {
    @Shadow @Final private RenderPhase.Target target;

    @Override
    public RenderPhase.Target drughack$getTarget() {
        return this.target;
    }
}
