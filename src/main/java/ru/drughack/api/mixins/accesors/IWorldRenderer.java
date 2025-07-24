package ru.drughack.api.mixins.accesors;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface IWorldRenderer {
    @Accessor("frustum")
    Frustum getFrustum();

    @Accessor("bufferBuilders")
    BufferBuilderStorage getBufferBuilders();

    @Accessor("framebufferSet")
    DefaultFramebufferSet getFrameBufferSet();

    @Accessor("entityOutlineFramebuffer")
    Framebuffer getEntityOutlineFramebuffer();
}