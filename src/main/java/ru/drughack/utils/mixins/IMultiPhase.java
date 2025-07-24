package ru.drughack.utils.mixins;

import net.minecraft.client.render.RenderLayer;

public interface IMultiPhase {
    RenderLayer.MultiPhaseParameters drughack$getParameters();
}