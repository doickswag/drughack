package ru.drughack.modules.impl.render;

import lombok.AllArgsConstructor;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IPostEffectProcessor;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.ColorSetting;

import java.awt.*;

public class Shaders extends Module {

    public Setting<Integer> lineWidth = new Setting<>("Line Width", 1, 0, 5);
    public Setting<Float> scale = new Setting<>("Scale", 0.5f, 0.1f, 10f);
    public Setting<Float> fillAlpha = new Setting<>("Fill Alpha", 0.6f, 0f, 1f);
    public Setting<Float> outlineAlpha = new Setting<>("Outline Alpha", 1f, 0f, 1f);
    public Setting<ColorSetting> mainColor = new Setting<>("Main Color", new ColorSetting(new ColorSetting.Colors(new Color(189, 153, 255), false)));
    public Setting<ColorSetting> offColor = new Setting<>("Off Color", new ColorSetting(new ColorSetting.Colors(new Color( 255, 255, 255), false)));
    public Setting<Float> speed = new Setting<>("Speed", 5f, 1f, 20f);
    public Setting<Boolean> hands = new Setting<>("Hands", true);
    public Setting<Boolean> crystals = new Setting<>("Crystals", true);
    public Setting<Boolean> players = new Setting<>("Players", true);
    public Setting<Boolean> self = new Setting<>("Self", true);
    public Setting<Boolean> items = new Setting<>("Items", true);

    public PostEffectProcessor gradientShader;

    public Shaders() {
        super("Shaders", "shader", Category.Render);
    }

    public void drawShader(FrameGraphBuilder builder, int textureWidth, int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet) {
        if (gradientShader != null) {
            ShaderProgram program = (((IPostEffectProcessor) gradientShader).getPasses().getFirst().getProgram());
            program.getUniform("lineWidth").set(lineWidth.getValue());
            program.getUniform("scale").set(scale.getValue() * 1000f);
            program.getUniform("time").set(DrugHack.getInstance().getShaderManager().getTimer().getPassedTime() / 1000f);
            program.getUniform("resolution").set((float) mc.getWindow().getWidth(), mc.getWindow().getHeight());
            program.getUniform("fillAlpha").set(fillAlpha.getValue());
            program.getUniform("outlineAlpha").set(outlineAlpha.getValue());
            program.getUniform("color1").set((float) mainColor.getValue().getColor().getRed() / 255f, (float) mainColor.getValue().getColor().getGreen() / 255f, (float) mainColor.getValue().getColor().getBlue() / 255f);
            program.getUniform("color2").set((float) offColor.getValue().getColor().getRed() / 255f, (float) offColor.getValue().getColor().getGreen() / 255f, (float) offColor.getValue().getColor().getBlue() / 255f);
            program.getUniform("speed").set(speed.getValue());
            gradientShader.render(builder, textureWidth, textureHeight, framebufferSet);
        }
    }

    public void loadShaders() {
        gradientShader = mc.getShaderLoader().loadPostEffect(Identifier.of("drughack", "gradient"), DefaultFramebufferSet.MAIN_AND_ENTITY_OUTLINE);
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Gradient("Gradient"),
        Shoreline("Shoreline");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}