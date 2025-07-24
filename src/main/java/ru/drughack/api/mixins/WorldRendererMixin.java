package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private BufferBuilderStorage bufferBuilders;
    @Shadow @Final private MinecraftClient client;
    @Unique boolean allowShader = false;

    @Inject(method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", at = @At("HEAD"), cancellable = true)
    private void hasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().blindness.getValue()) info.setReturnValue(false);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean render$setupTerrain(boolean spectator) {
        return DrugHack.getInstance().getModuleManager().getFreecam().isToggled() || spectator;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(Lnet/minecraft/client/render/FrameGraphBuilder;IILnet/minecraft/client/gl/PostEffectProcessor$FramebufferSet;)V", ordinal = 0))
    public void modifyRenderOutlinePostProcessor(PostEffectProcessor instance, FrameGraphBuilder builder, int textureWidth, int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet) {
        if (!DrugHack.getInstance().getModuleManager().getShaders().isToggled()) instance.render(builder, textureWidth, textureHeight, framebufferSet);
    }

    @Redirect(method = "getEntitiesToRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
    public boolean modifyHasOutline(MinecraftClient instance, Entity entity) {
        if (DrugHack.getInstance().getModuleManager().getShaders().isToggled()) return true;
        else return instance.hasOutline(entity);
    }

    @ModifyArg(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"), index = 6)
    public VertexConsumerProvider modifyArgRenderEntity(VertexConsumerProvider vertexConsumers, @Local Entity entityLocalRef) {
        if (DrugHack.getInstance().getModuleManager().getShaders().isToggled() && isGavno$$$(entityLocalRef)) {
            allowShader = true;
            OutlineVertexConsumerProvider outlineVertexConsumerProvider = bufferBuilders.getOutlineVertexConsumers();
            int i = entityLocalRef.getTeamColorValue();
            outlineVertexConsumerProvider.setColor(ColorHelper.getRed(i), ColorHelper.getGreen(i), ColorHelper.getBlue(i), 255);
            return outlineVertexConsumerProvider;
        } else {
            allowShader = false;
            return vertexConsumers;
        }
    }

    @Inject(method = "canDrawEntityOutlines", at = @At("HEAD"), cancellable = true)
    public void modifyCanDrawEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if (DrugHack.getInstance().getModuleManager().getShaders().isToggled()) cir.setReturnValue(true);
    }

    @Unique
    public boolean isGavno$$$(Entity entity) {
        boolean value = DrugHack.getInstance().getModuleManager().getShaders().players.getValue() && entity instanceof PlayerEntity && entity != client.player;
        if (DrugHack.getInstance().getModuleManager().getShaders().self.getValue() && entity == client.player) value = true;
        if (DrugHack.getInstance().getModuleManager().getShaders().items.getValue() && entity instanceof ItemEntity) value = true;
        if (DrugHack.getInstance().getModuleManager().getShaders().crystals.getValue() && entity instanceof EndCrystalEntity) value = true;
        return value;
    }
}