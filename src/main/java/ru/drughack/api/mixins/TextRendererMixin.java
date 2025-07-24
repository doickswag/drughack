package ru.drughack.api.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

    @Inject(method = "drawLayer(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)F", at = @At("HEAD"), cancellable = true)
    private void drawLayer$String(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int backgroundColor, int light, boolean swapZIndex, CallbackInfoReturnable<Float> info) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && DrugHack.getInstance().getModuleManager().getFontModule().global.getValue()) {
            MatrixStack matrices = new MatrixStack();

            matrices.push();
            matrices.multiplyPositionMatrix(matrix);

            if (shadow) DrugHack.getInstance().getFontManager().getFontRenderer().drawString(matrices, text, x + DrugHack.getInstance().getFontManager().getShadowOffset(), y + DrugHack.getInstance().getFontManager().getShadowOffset(), color, true);
            DrugHack.getInstance().getFontManager().getFontRenderer().drawString(matrices, text, x, y, color, false);

            matrices.pop();

            info.setReturnValue(x + DrugHack.getInstance().getFontManager().getFontRenderer().getTextWidth(text));
        }
    }

    @Inject(method = "drawLayer(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)F", at = @At("HEAD"), cancellable = true)
    private void drawLayer$OrderedText(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int underlineColor, int light, boolean swapZIndex, CallbackInfoReturnable<Float> info) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && DrugHack.getInstance().getModuleManager().getFontModule().global.getValue()) {
            MatrixStack matrices = new MatrixStack();

            matrices.push();
            matrices.multiplyPositionMatrix(matrix);

            if (shadow) DrugHack.getInstance().getFontManager().getFontRenderer().drawText(matrices, text, x + DrugHack.getInstance().getFontManager().getShadowOffset(), y + DrugHack.getInstance().getFontManager().getShadowOffset(), color, true);
            DrugHack.getInstance().getFontManager().getFontRenderer().drawText(matrices, text, x, y, color, false);

            matrices.pop();

            info.setReturnValue(x + DrugHack.getInstance().getFontManager().getFontRenderer().getTextWidth(text));
        }
    }

    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void getWidth(String text, CallbackInfoReturnable<Integer> info) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && DrugHack.getInstance().getModuleManager().getFontModule().global.getValue()) {
            info.setReturnValue(DrugHack.getInstance().getFontManager().getWidth(text));
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)I", at = @At("HEAD"), cancellable = true)
    private void getWidth(StringVisitable text, CallbackInfoReturnable<Integer> info) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && DrugHack.getInstance().getModuleManager().getFontModule().global.getValue()) {
            info.setReturnValue(DrugHack.getInstance().getFontManager().getWidth(text.getString()));
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)I", at = @At("HEAD"), cancellable = true)
    private void getWidth(OrderedText text, CallbackInfoReturnable<Integer> info) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && DrugHack.getInstance().getModuleManager().getFontModule().global.getValue()) {
            info.setReturnValue((int) DrugHack.getInstance().getFontManager().getFontRenderer().getTextWidth(text));
        }
    }
}