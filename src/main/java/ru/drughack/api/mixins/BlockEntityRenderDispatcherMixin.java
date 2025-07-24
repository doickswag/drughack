package ru.drughack.api.mixins;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements Wrapper {
    
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
    private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && !DrugHack.getInstance().getModuleManager().getNoRender().tileEntities.getValue().name().equals("None")) {
            if (DrugHack.getInstance().getModuleManager().getNoRender().tileEntities.getValue().name().equals("Always") || (DrugHack.getInstance().getModuleManager().getNoRender().tileEntities.getValue().name().equals("Distance") && Math.sqrt(mc.player.squaredDistanceTo(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ())) > DrugHack.getInstance().getModuleManager().getNoRender().tileDistance.getValue().floatValue())) {
                info.cancel();
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void render(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && !DrugHack.getInstance().getModuleManager().getNoRender().tileEntities.getValue().equals("None")) {
            if (DrugHack.getInstance().getModuleManager().getNoRender().tileEntities.getValue().name().equals("Always") || (DrugHack.getInstance().getModuleManager().getNoRender().tileEntities.getValue().name().equals("Distance") && Math.sqrt(mc.player.squaredDistanceTo(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ())) > DrugHack.getInstance().getModuleManager().getNoRender().tileDistance.getValue().floatValue())) {
                info.cancel();
            }
        }
    }
}