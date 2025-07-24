package ru.drughack.api.mixins.accesors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(DrawContext.class)
public interface IDrawContext {

    @Accessor("vertexConsumers")
    VertexConsumerProvider.Immediate getVertexConsumers();
}
