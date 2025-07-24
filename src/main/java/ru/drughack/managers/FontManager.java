package ru.drughack.managers;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IDrawContext;
import ru.drughack.api.mixins.accesors.ITextRenderer;
import ru.drughack.modules.impl.client.FontModule;
import ru.drughack.utils.fonts.FontRenderer;
import ru.drughack.utils.interfaces.Wrapper;

import java.awt.*;

@Getter @Setter
public class FontManager implements Wrapper {
    private FontRenderer fontRenderer;

    public void drawText(DrawContext context, String text, int x, int y, Color color) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            fontRenderer.drawString(context.getMatrices(), text, x, y, color.getRGB(), false);
        } else {
            context.drawText(mc.textRenderer, text, x, y, color.getRGB(), false);
        }
    }

    public void drawTextWithShadow(DrawContext context, String text, int x, int y, Color color) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            if (!(DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.None)) fontRenderer.drawString(context.getMatrices(), text, x + getShadowOffset(), y + getShadowOffset(), color.getRGB(), true);
            fontRenderer.drawString(context.getMatrices(), text, x, y, color.getRGB(), false);
        } else {
            context.drawText(mc.textRenderer, text, x, y, color.getRGB(), true);
        }
    }

    public void drawTextWithShadow(DrawContext context, String text, float x, float y, int color) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            if (!(DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.None)) fontRenderer.drawString(context.getMatrices(), text, x + getShadowOffset(), y + getShadowOffset(), color, true);
            fontRenderer.drawString(context.getMatrices(), text, x, y, color, false);
        } else {
            context.drawText(mc.textRenderer, text, (int) x, (int) y, color, true);
        }
    }

    public void drawText(DrawContext context, OrderedText text, int x, int y, Color color) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            fontRenderer.drawText(context.getMatrices(), text, x, y, color.getRGB(), false);
        } else {
            context.drawText(mc.textRenderer, text, x, y, color.getRGB(), false);
        }
    }

    public void drawTextWithShadow(DrawContext context, OrderedText text, int x, int y, Color color) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            if (!(DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.None)) fontRenderer.drawText(context.getMatrices(), text, x + getShadowOffset(), y + getShadowOffset(), color.getRGB(), true);
            fontRenderer.drawText(context.getMatrices(), text, x, y, color.getRGB(), false);
        } else {
            context.drawText(mc.textRenderer, text, x, y, color.getRGB(), true);
        }
    }

    public void drawTextWithShadow(MatrixStack matrices, String text, int x, int y, VertexConsumerProvider vertexConsumers, Color color) {
        RenderSystem.disableDepthTest();

        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            if (!(DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.None)) fontRenderer.drawString(matrices, text, x + getShadowOffset(), y + getShadowOffset(), color.getRGB(), true);
            fontRenderer.drawString(matrices, text, x, y, color.getRGB(), false);
        } else {
            ((ITextRenderer) mc.textRenderer).invokeDrawLayer(text, x, y, ITextRenderer.invokeTweakTransparency(color.getRGB()), true, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0, false);
            mc.getBufferBuilders().getEntityVertexConsumers().draw();

            ((ITextRenderer) mc.textRenderer).invokeDrawLayer(text, x, y, ITextRenderer.invokeTweakTransparency(color.getRGB()), false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0, false);
            mc.getBufferBuilders().getEntityVertexConsumers().draw();
        }

        RenderSystem.enableDepthTest();
    }

    public void drawTextWithOutline(DrawContext context, String text, int x, int y, Color color, Color outlineColor) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            fontRenderer.drawString(context.getMatrices(), FontRenderer.stripControlCodes(text), x + 0.5f, y - 0.5f, outlineColor.getRGB(), false);
            fontRenderer.drawString(context.getMatrices(), FontRenderer.stripControlCodes(text), x - 0.5f, y + 0.5f, outlineColor.getRGB(), false);
            fontRenderer.drawString(context.getMatrices(), FontRenderer.stripControlCodes(text), x + 0.5f, y + 0.5f, outlineColor.getRGB(), false);
            fontRenderer.drawString(context.getMatrices(), FontRenderer.stripControlCodes(text), x - 0.5f, y - 0.5f, outlineColor.getRGB(), false);

            fontRenderer.drawString(context.getMatrices(), text, x, y, color.getRGB(), false);
        } else {
            mc.textRenderer.drawWithOutline(Text.literal(text).asOrderedText(), mc.getWindow().getScaledWidth() / 2.0f - DrugHack.getInstance().getFontManager().getWidth(text) / 2.0f, mc.getWindow().getScaledHeight() / 2.0f + 16, color.getRGB(), outlineColor.getRGB(), context.getMatrices().peek().getPositionMatrix(), ((IDrawContext) context).getVertexConsumers(), 0);
        }
    }

    public int getWidth(String text) {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            return (int) fontRenderer.getTextWidth(text) + DrugHack.getInstance().getModuleManager().getFontModule().widthOffset.getValue();
        } else {
            return mc.textRenderer.getWidth(text);
        }
    }

    public int getHeight() {
        if (DrugHack.getInstance().getModuleManager().getFontModule().customFont.getValue() && fontRenderer != null) {
            return (int) fontRenderer.getHeight() + DrugHack.getInstance().getModuleManager().getFontModule().heightOffset.getValue();
        } else {
            return mc.textRenderer.fontHeight;
        }
    }

    public float getShadowOffset() {
        if (DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.None) {
            return 0.0f;
        } else if (DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.Custom) {
            return DrugHack.getInstance().getModuleManager().getFontModule().shadowOffset.getValue();
        } else {
            return 1.0f;
        }
    }
}
