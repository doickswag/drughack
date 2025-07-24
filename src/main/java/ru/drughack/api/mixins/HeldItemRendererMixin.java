package ru.drughack.api.mixins;

import com.google.common.base.MoreObjects;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.modules.impl.render.ViewModel;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.mixins.IHeldItemRenderer;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin implements Wrapper, IHeldItemRenderer {

    @Shadow @Final private MinecraftClient client;
    @Unique private final ViewModel viewModel = DrugHack.getInstance().getModuleManager().getViewModel();
    @Shadow private ItemStack mainHand;
    @Shadow private float equipProgressMainHand;
    @Shadow private float prevEquipProgressMainHand;
    @Shadow protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
    @Shadow private float prevEquipProgressOffHand;
    @Shadow private float equipProgressOffHand;
    @Shadow private ItemStack offHand;
    @Shadow static HeldItemRenderer.HandRenderType getHandRenderType(ClientPlayerEntity player) {
        return null;
    }

    @Redirect(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getLerpedPitch(F)F"))
    public float modifyGetLerpedPitchInRenderItem(ClientPlayerEntity instance, float tickDelta) {
        return DrugHack.getInstance().getRotationManager().isEmpty() ? instance.getLerpedPitch(tickDelta) : MathHelper.lerp(tickDelta, DrugHack.getInstance().getRotationManager().getRotationData().getPrevPitch(), DrugHack.getInstance().getRotationManager().getRotationData().getPitch());
    }

    @Redirect(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch(F)F"))
    public float modifyGetPitchInRenderItem(ClientPlayerEntity instance, float tickDelta) {
        return DrugHack.getInstance().getRotationManager().isEmpty() ? instance.getPitch(tickDelta) : DrugHack.getInstance().getRotationManager().getRotationData().getPitch();
    }

    @Redirect(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw(F)F"))
    public float modifyGetYawInRenderItem(ClientPlayerEntity instance, float tickDelta) {
        return DrugHack.getInstance().getRotationManager().isEmpty() ? instance.getYaw(tickDelta) : DrugHack.getInstance().getRotationManager().getRotationData().getYaw();
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if (viewModel.isToggled()) {
            boolean bl = client.player.getActiveItem().getItem() == item.getItem();
            boolean bl2 = item.contains(DataComponentTypes.FOOD) || item.getItem() instanceof PotionItem;

            if (hand == Hand.MAIN_HAND) {
                float f6 = bl2 && bl ? 0.0f : -viewModel.mainX.getValue();
                float f5 = bl2 && bl ? 0.0f : viewModel.mainZ.getValue();
                if (client.player.getMainArm() == Arm.LEFT) f6 = -f6;
                matrices.translate(f6, viewModel.mainY.getValue(), f5);
                matrices.scale(viewModel.mainScaleX.getValue(), viewModel.mainScaleY.getValue(), viewModel.mainScaleZ.getValue());
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(viewModel.mainRotateX.getValue()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(viewModel.mainRotateY.getValue()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(viewModel.mainRotateZ.getValue()));
            } else {
                float f9 = bl2 && bl ? 0.0f : viewModel.offX.getValue();
                float f8 = bl2 && bl ? 0.0f : viewModel.offZ.getValue();
                if (client.player.getMainArm() == Arm.LEFT) f9 = -f9;
                matrices.translate(f9, viewModel.offY.getValue(), f8);
                matrices.scale(viewModel.offScaleX.getValue(), viewModel.offScaleY.getValue(), viewModel.offScaleZ.getValue());
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(viewModel.offRotateX.getValue()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(viewModel.offRotateY.getValue()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(viewModel.offRotateZ.getValue()));
            }
        }
    }

    @ModifyArgs(method = "applyEquipOffset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    private void applyEquipOffsetHook(Args args) {
        if (viewModel.isToggled() && viewModel.instantSwap.getValue()) args.set(1, -0.52f);
    }

    @ModifyArgs(method = "applyEatOrDrinkTransformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void applyEatOrDrinkTransformationHook(Args args) {
        if (viewModel.isToggled()) args.set(1, ((Float) args.get(1) * viewModel.eatMultiplier.getValue()));
    }

    @Override
    public void renderShaderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, ClientPlayerEntity player, int light) {
        float f = player.getHandSwingProgress(tickDelta);
        Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
        float g = player.getLerpedPitch(tickDelta);
        HeldItemRenderer.HandRenderType handRenderType = getHandRenderType(player);
        float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
        float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((player.getPitch(tickDelta) - h) * 0.1F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((player.getYaw(tickDelta) - i) * 0.1F));
        float j;
        float k;

        if (handRenderType.renderMainHand) {
            j = hand == Hand.MAIN_HAND ? f : 0.0F;
            k = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressMainHand, this.equipProgressMainHand);
            this.renderFirstPersonItem(player, tickDelta, g, Hand.MAIN_HAND, j, this.mainHand, k, matrices, vertexConsumers, light);
        }

        if (handRenderType.renderOffHand) {
            j = hand == Hand.OFF_HAND ? f : 0.0F;
            k = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressOffHand, this.equipProgressOffHand);
            this.renderFirstPersonItem(player, tickDelta, g, Hand.OFF_HAND, j, this.offHand, k, matrices, vertexConsumers, light);
        }
    }
}