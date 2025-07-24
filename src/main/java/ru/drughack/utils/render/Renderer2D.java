package ru.drughack.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import ru.drughack.utils.interfaces.Wrapper;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer2D implements Wrapper {

    public static void renderQuad(MatrixStack matrices, float left, float top, float right, float bottom, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, left, top, 0.0f).color(color.getRGB());
        buffer.vertex(matrix, left, bottom, 0.0f).color(color.getRGB());
        buffer.vertex(matrix, right, bottom, 0.0f).color(color.getRGB());
        buffer.vertex(matrix, right, top, 0.0f).color(color.getRGB());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderQuad(MatrixStack matrices, float left, float top, float right, float bottom, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, left, top, 0.0f).color(color);
        buffer.vertex(matrix, left, bottom, 0.0f).color(color);
        buffer.vertex(matrix, right, bottom, 0.0f).color(color);
        buffer.vertex(matrix, right, top, 0.0f).color(color);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, left, top, 0.0f).color(startColor.getRGB());
        buffer.vertex(matrix, left, bottom, 0.0f).color(endColor.getRGB());
        buffer.vertex(matrix, right, bottom, 0.0f).color(endColor.getRGB());
        buffer.vertex(matrix, right, top, 0.0f).color(startColor.getRGB());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderSidewaysGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, left, top, 0.0f).color(startColor.getRGB());
        buffer.vertex(matrix, left, bottom, 0.0f).color(startColor.getRGB());
        buffer.vertex(matrix, right, bottom, 0.0f).color(endColor.getRGB());
        buffer.vertex(matrix, right, top, 0.0f).color(endColor.getRGB());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderOutline(MatrixStack matrices, float left, float top, float right, float bottom, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int colorRGB = color.getRGB();
        buffer.vertex(matrix, left, top + 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left, bottom - 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left + 1f, bottom - 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left + 1f, top + 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right - 1f, top + 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right - 1f, bottom - 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right, bottom - 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right, top + 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left, top, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left, top + 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right, top + 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right, top, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left, bottom - 1f, 0.0f).color(colorRGB);
        buffer.vertex(matrix, left, bottom, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right, bottom, 0.0f).color(colorRGB);
        buffer.vertex(matrix, right, bottom - 1f, 0.0f).color(colorRGB);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderLine(MatrixStack matrices, float x, float y, float targetX, float targetY, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y, 0.0f).color(color.getRGB());
        buffer.vertex(matrix, targetX, targetY, 0.0f).color(color.getRGB());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderCircle(MatrixStack matrices, float x, float y, float radius, Color color) {
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; ++i) buffer.vertex(matrices.peek().getPositionMatrix(), (float) (x + Math.sin((double) i * 3.141526 / 180.0) * (double) radius), (float) (y + Math.cos((double) i * 3.141526 / 180.0) * (double) radius), 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderTexture(MatrixStack matrices, float left, float top, float right, float bottom, Identifier identifier, Color color) {
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrices.peek().getPositionMatrix(), left, top, 0).texture(0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrices.peek().getPositionMatrix(), left, bottom, 0).texture(0, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrices.peek().getPositionMatrix(), right, bottom, 0).texture(1, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrices.peek().getPositionMatrix(), right, top, 0).texture(1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, identifier);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void renderTexture(MatrixStack matrices, float left, float top, float right, float bottom, AbstractTexture texture, Color color) {
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrices.peek().getPositionMatrix(), left, top, 0).texture(0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrices.peek().getPositionMatrix(), left, bottom, 0).texture(0, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrices.peek().getPositionMatrix(), right, bottom, 0).texture(1, 1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrices.peek().getPositionMatrix(), right, top, 0).texture(1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture.getGlId());
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static AbstractTexture convertToTexture(BufferedImage image) {
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            NativeImage nativeImage = new NativeImage(width, height, false);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    nativeImage.setColorArgb(x, y, rgb);
                }
            }

            return new NativeImageBackedTexture(nativeImage);
        }

        return null;
    }
}