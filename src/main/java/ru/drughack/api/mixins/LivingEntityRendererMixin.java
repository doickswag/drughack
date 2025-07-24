package ru.drughack.api.mixins;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.mixins.IILivingEntity;
import ru.drughack.utils.mixins.ILivingEntityRenderer;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements ILivingEntityRenderer, Wrapper {

    @Shadow protected M model;
    @Shadow protected abstract int getMixColor(S state);
    @Shadow protected abstract float getAnimationCounter(S state);
    @Shadow @Nullable protected abstract RenderLayer getRenderLayer(S state, boolean showBody, boolean translucent, boolean showOutline);
    @Shadow protected abstract boolean isVisible(S state);
    @Shadow protected abstract void setupTransforms(S state, MatrixStack matrices, float animationProgress, float bodyYaw);
    @Shadow protected abstract void scale(S state, MatrixStack matrices);
    @Shadow private static float clampBodyYaw(LivingEntity entity, float degrees, float tickDelta) {
        return 0;
    }
    @Shadow public static boolean shouldFlipUpsideDown(LivingEntity entity) {
        return false;
    }
    public LivingEntityRendererMixin(EntityRendererFactory.Context context) { super(context); }

    /**
     * @author serverattacked
     * @reason idk
     */

    @Overwrite
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f) {
        if (((IILivingEntity) livingEntity).drughack$isStaticPlayerEntity()) f = 1;

        super.updateRenderState(livingEntity, livingEntityRenderState, f);

        float g = MathHelper.lerpAngleDegrees(f, livingEntity.prevHeadYaw, livingEntity.headYaw);

//        if (livingEntity == mc.player && DrugHack.getInstance().getRotationManager().getRotationData() != null) {
//            livingEntityRenderState.bodyYaw = DrugHack.getInstance().getRotationManager().getRenderRotations()[0];
//            livingEntityRenderState.yawDegrees = MathHelper.wrapDegrees(DrugHack.getInstance().getRotationManager().getRenderRotations()[0] - livingEntityRenderState.bodyYaw);
//            livingEntityRenderState.pitch = DrugHack.getInstance().getRotationManager().getRenderRotations()[1];
//        } else {
            livingEntityRenderState.bodyYaw = clampBodyYaw(livingEntity, g, f);
            livingEntityRenderState.yawDegrees = MathHelper.wrapDegrees(g - livingEntityRenderState.bodyYaw);
            livingEntityRenderState.pitch = livingEntity.getLerpedPitch(f);
//        }

        livingEntityRenderState.customName = livingEntity.getCustomName();
        livingEntityRenderState.flipUpsideDown = shouldFlipUpsideDown(livingEntity);
        if (livingEntityRenderState.flipUpsideDown) {
            livingEntityRenderState.pitch *= -1.0F;
            livingEntityRenderState.yawDegrees *= -1.0F;
        }

        if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
            livingEntityRenderState.limbFrequency = livingEntity.limbAnimator.getPos(f);
            livingEntityRenderState.limbAmplitudeMultiplier = livingEntity.limbAnimator.getSpeed(f);
        } else {
            livingEntityRenderState.limbFrequency = 0.0F;
            livingEntityRenderState.limbAmplitudeMultiplier = 0.0F;
        }

        Entity entity = livingEntity.getVehicle();
        if (entity instanceof LivingEntity livingEntity2) livingEntityRenderState.headItemAnimationProgress = livingEntity2.limbAnimator.getPos(f);
        else livingEntityRenderState.headItemAnimationProgress = livingEntityRenderState.limbFrequency;

        livingEntityRenderState.baseScale = livingEntity.getScale();
        livingEntityRenderState.ageScale = livingEntity.getScaleFactor();
        livingEntityRenderState.pose = livingEntity.getPose();
        livingEntityRenderState.sleepingDirection = livingEntity.getSleepingDirection();
        if (livingEntityRenderState.sleepingDirection != null) livingEntityRenderState.standingEyeHeight = livingEntity.getEyeHeight(EntityPose.STANDING);

        livingEntityRenderState.shaking = livingEntity.isFrozen();
        livingEntityRenderState.baby = livingEntity.isBaby();
        livingEntityRenderState.touchingWater = livingEntity.isTouchingWater();
        livingEntityRenderState.usingRiptide = livingEntity.isUsingRiptide();
        livingEntityRenderState.hurt = livingEntity.hurtTime > 0 || livingEntity.deathTime > 0;
        livingEntityRenderState.deathTime = livingEntity.deathTime > 0 ? (float) livingEntity.deathTime + f : 0.0F;
        livingEntityRenderState.invisibleToPlayer = livingEntityRenderState.invisible && livingEntity.isInvisibleTo(mc.player);
        livingEntityRenderState.hasOutline = mc.hasOutline(livingEntity);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().corpses.getValue() && livingEntityRenderState.deathTime > 0) {
            info.cancel();
        }
    }

    @Override
    public void drughack$render(LivingEntityRenderState livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().corpses.getValue() && livingEntityRenderState.deathTime > 0) return;

        matrixStack.push();

        if (livingEntityRenderState.isInPose(EntityPose.SLEEPING)) {
            Direction direction = livingEntityRenderState.sleepingDirection;
            if (direction != null) {
                float f = livingEntityRenderState.standingEyeHeight - 0.1F;
                matrixStack.translate((float)(-direction.getOffsetX()) * f, 0.0F, (float)(-direction.getOffsetZ()) * f);
            }
        }

        float g = livingEntityRenderState.baseScale;
        matrixStack.scale(g, g, g);
        this.setupTransforms((S) livingEntityRenderState, matrixStack, livingEntityRenderState.bodyYaw, g);
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale((S) livingEntityRenderState, matrixStack);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
        this.model.setAngles((S) livingEntityRenderState);
        boolean bl = this.isVisible((S) livingEntityRenderState);
        boolean bl2 = !bl && !livingEntityRenderState.invisibleToPlayer;

        RenderLayer renderLayer = this.getRenderLayer((S) livingEntityRenderState, bl, bl2, livingEntityRenderState.hasOutline);
        if (renderLayer != null) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            int j = LivingEntityRenderer.getOverlay(livingEntityRenderState, this.getAnimationCounter((S) livingEntityRenderState));
            int k = bl2 ? 654311423 : -1;
            int l = ColorHelper.mix(k, this.getMixColor((S) livingEntityRenderState));
            this.model.render(matrixStack, vertexConsumer, i, j, l);
        }

        matrixStack.pop();
    }
}