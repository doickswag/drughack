package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.api.mixins.accesors.IGameRenderer;
import ru.drughack.api.mixins.accesors.IPostEffectProcessor;
import ru.drughack.api.mixins.accesors.IWorldRenderer;
import ru.drughack.modules.impl.render.Shaders;
import ru.drughack.utils.mixins.IHeldItemRenderer;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.world.WorldUtils;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean renderingPanorama;
    @Shadow protected abstract float getFov(Camera camera, float tickDelta, boolean changingFov);
    @Shadow protected abstract void tiltViewWhenHurt(MatrixStack matrices, float tickDelta);
    @Shadow @Final private LightmapTextureManager lightmapTextureManager;
    @Shadow @Final public HeldItemRenderer firstPersonRenderer;
    @Shadow public abstract Matrix4f getBasicProjectionMatrix(float fovDegrees);
    @Shadow @Final private BufferBuilderStorage buffers;
    @Shadow protected abstract void bobView(MatrixStack matrices, float tickDelta);

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().hurtCamera.getValue()) info.cancel();
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void showFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().totemAnimation.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void renderWorld$HEAD(RenderTickCounter tickCounter, CallbackInfo info) {
        Renderer3D.prepare();
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void renderWorld$swap(RenderTickCounter tickCounter, CallbackInfo info, @Local(ordinal = 2) Matrix4f matrix4f3, @Local(ordinal = 1) float tickDelta, @Local MatrixStack matrixStack) {
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(matrix4f3);
        RenderSystem.getModelViewStack().mul(matrixStack.peek().getPositionMatrix().invert());
        DrugHack.getInstance().getEventHandler().post(new EventRender3D(matrixStack, tickDelta));
        Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
        Renderer3D.draw(Renderer3D.SHINE_QUADS, Renderer3D.SHINE_DEBUG_LINES, true);
        DrugHack.getInstance().getEventHandler().post(new EventRender3D.Post(matrixStack, tickDelta));
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"), cancellable = true)
    private void updateCrosshairTarget(float tickDelta, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getFreecam().isToggled()) {
            Profilers.get().pop();
            client.crosshairTarget = WorldUtils.getRaytraceTarget(DrugHack.getInstance().getModuleManager().getFreecam().getFreeYaw(), DrugHack.getInstance().getModuleManager().getFreecam().getFreePitch(), DrugHack.getInstance().getModuleManager().getFreecam().getFreeX(), DrugHack.getInstance().getModuleManager().getFreecam().getFreeY(), DrugHack.getInstance().getModuleManager().getFreecam().getFreeZ());
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    private @Nullable EntityHitResult findCrosshairTarget(@Nullable EntityHitResult original) {
        if (DrugHack.getInstance().getModuleManager().getReach().isToggled() && DrugHack.getInstance().getModuleManager().getReach().noEntityTrace.getValue()) return null;

        return original;
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    public void modifyRenderHandPost(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        Shaders shaders = DrugHack.getInstance().getModuleManager().getShaders();
        if (shaders.isToggled()) {
            DefaultFramebufferSet framebufferSet = ((IWorldRenderer) client.worldRenderer).getFrameBufferSet();
            FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
            framebufferSet.mainFramebuffer = frameGraphBuilder.createObjectNode("main", client.getFramebuffer());
            framebufferSet.entityOutlineFramebuffer = frameGraphBuilder.createObjectNode("entity_outline", ((IWorldRenderer) client.worldRenderer).getEntityOutlineFramebuffer());
            int frameBufferWith = client.getFramebuffer().textureWidth;
            int frameBufferHeight = client.getFramebuffer().textureHeight;
            RenderPass renderPass = frameGraphBuilder.createPass("main");
            framebufferSet.mainFramebuffer = renderPass.transfer(framebufferSet.mainFramebuffer);
            framebufferSet.entityOutlineFramebuffer = renderPass.transfer(framebufferSet.entityOutlineFramebuffer);
            renderPass.setRenderer(() -> {
                framebufferSet.entityOutlineFramebuffer.get().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                framebufferSet.entityOutlineFramebuffer.get().beginWrite(false);
                renderShaderHand(camera, tickDelta);
                ((IWorldRenderer) client.worldRenderer).getBufferBuilders().getOutlineVertexConsumers().draw();
            });
            shaders.loadShaders();
            shaders.drawShader(frameGraphBuilder, frameBufferWith, frameBufferHeight, framebufferSet);
            frameGraphBuilder.run(((IGameRenderer) client.gameRenderer).getPool());
            client.getFramebuffer().beginWrite(false);
            framebufferSet.clear();
            if (shaders.hands.getValue()) ci.cancel();
        }
    }

    @Unique
    public void renderShaderHand(Camera camera, float tickDelta) {
        if (!renderingPanorama) {
            Matrix4f matrix4f2 = getBasicProjectionMatrix(this.getFov(camera, tickDelta, false));
            RenderSystem.setProjectionMatrix(matrix4f2, ProjectionType.PERSPECTIVE);
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.push();
            tiltViewWhenHurt(matrixStack, tickDelta);
            if (client.options.getBobView().getValue()) bobView(matrixStack, tickDelta);
            boolean bl = client.getCameraEntity() instanceof LivingEntity && ((LivingEntity) client.getCameraEntity()).isSleeping();
            if (client.options.getPerspective().isFirstPerson() && !bl && !client.options.hudHidden && client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
                lightmapTextureManager.enable();
                OutlineVertexConsumerProvider outlineVertexConsumerProvider = ((IWorldRenderer) client.worldRenderer).getBufferBuilders().getOutlineVertexConsumers();
                ((IHeldItemRenderer) firstPersonRenderer).renderShaderItem(tickDelta, matrixStack, outlineVertexConsumerProvider, client.player, client.getEntityRenderDispatcher().getLight(client.player, tickDelta));
                lightmapTextureManager.disable();
            }

            matrixStack.pop();
            if (client.options.getPerspective().isFirstPerson() && !bl) {
                VertexConsumerProvider.Immediate immediate = buffers.getEntityVertexConsumers();
                InGameOverlayRenderer.renderOverlays(client, matrixStack, immediate);
                immediate.draw();
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void modifyRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        DrugHack.getInstance().getShaderManager().updateTime();
    }

    @ModifyReturnValue(method = "getFov",at = @At("RETURN"))
    public float modifyGetFov(float original) {
        return DrugHack.getInstance().getModuleManager().getZoom().isToggled() ? DrugHack.getInstance().getModuleManager().getZoom().getFov(original) : original;
    }
}