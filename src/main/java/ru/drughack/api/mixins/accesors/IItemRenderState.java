package ru.drughack.api.mixins.accesors;

import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderState.class)
public interface IItemRenderState {

    @Accessor("layerCount")
    int getLayerCount();

    @Accessor("layers")
    ItemRenderState.LayerRenderState[] getLayers();
}
