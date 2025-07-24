package ru.drughack.utils.mixins;

import net.minecraft.client.render.RenderPhase;

public interface IMultiPhaseParameters {
    RenderPhase.Target drughack$getTarget();
}