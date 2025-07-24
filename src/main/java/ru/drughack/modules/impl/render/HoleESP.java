package ru.drughack.modules.impl.render;

import lombok.AllArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.CategorySetting;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.world.HoleUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HoleESP extends Module {

    public Setting<Integer> range = new Setting<>("Range", 10, 1, 50);
    public Setting<Boolean> asynchronous = new Setting<>("Asynchronous", true);
    public Setting<Boolean> doubleHoles = new Setting<>("DoubleHoles", true);
    public Setting<Boolean> quadHoles = new Setting<>("QuadHoles", true);
    public Setting<Fill> fill = new Setting<>("Fill", Fill.Normal);
    public Setting<Float> fillHeight = new Setting<>("FillHeight", 1.0f, -2.0f, 2.0f);
    public Setting<Outline> outline = new Setting<>("Outline", Outline.Normal);
    public Setting<Float> outlineHeight = new Setting<>("OutlineHeight", 1.0f, -2.0f, 2.0f);

    public Setting<CategorySetting> bedrockColors = new Setting<>("Bedrock Colors", new CategorySetting());
    public Setting<ColorSetting> bedrockFill = new Setting<>("Fill", new ColorSetting(
            new ColorSetting.Colors(new Color(0, 255, 0, 50), false)),
            v -> bedrockColors.getValue().isOpen()
    );

    public Setting<ColorSetting> bedrockOutline = new Setting<>("Outline", new ColorSetting(
            new ColorSetting.Colors(new Color(0, 255, 0, 255), false)),
            v -> bedrockColors.getValue().isOpen()
    );


    public Setting<CategorySetting> mixedColors = new Setting<>("Mixed Colors", new CategorySetting());
    public Setting<ColorSetting> mixedFill = new Setting<>("Fill", new ColorSetting(
            new ColorSetting.Colors(new Color(255, 174, 0, 50), false)),
            v -> mixedColors.getValue().isOpen()
    );

    public Setting<ColorSetting> mixedOutline = new Setting<>("Outline", new ColorSetting(
            new ColorSetting.Colors(new Color(255, 174, 0, 255), false)),
            v -> mixedColors.getValue().isOpen()
    );

    public Setting<CategorySetting> obsidianColors = new Setting<>("Obsidian Colors", new CategorySetting());
    public Setting<ColorSetting> obsidianFill = new Setting<>("Fill", new ColorSetting(
            new ColorSetting.Colors(new Color(255, 0, 0, 50), false)),
            v -> obsidianColors.getValue().isOpen()
    );

    public Setting<ColorSetting> obsidianOutline = new Setting<>("Outline", new ColorSetting(
            new ColorSetting.Colors(new Color(255, 0, 0, 255), false)),
            v -> obsidianColors.getValue().isOpen()
    );

    public HoleESP() {
        super("HoleESP", "Renderer the hole", Category.Render);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<HoleUtils.Hole> holes = Collections.synchronizedList(new ArrayList<>());

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        List<BlockPos> sphere = new ArrayList<>();
        for (int i = 0; i < DrugHack.getInstance().getWorldManager().getRadius(range.getValue().doubleValue()); i++) {
            sphere.add(mc.player.getBlockPos().add(DrugHack.getInstance().getWorldManager().getOffset(i)));
        }

        Runnable runnable = () -> {
            List<HoleUtils.Hole> holes = new ArrayList<>();
            for (BlockPos position : sphere) {
                HoleUtils.Hole singleHole = HoleUtils.getSingleHole(position, 0);
                if (singleHole != null) {
                    holes.add(singleHole);
                    continue;
                }

                if (doubleHoles.getValue()) {
                    HoleUtils.Hole doubleHole = HoleUtils.getDoubleHole(position, 0);
                    if (doubleHole != null) {
                        holes.add(doubleHole);
                        continue;
                    }
                }

                if (quadHoles.getValue()) {
                    HoleUtils.Hole quadHole = HoleUtils.getQuadHole(position, 0);
                    if (quadHole != null) {
                        holes.add(quadHole);
                    }
                }
            }

            synchronized (this.holes) {
                this.holes.clear();
                this.holes.addAll(holes);
            }
        };

        if (asynchronous.getValue()) executor.submit(runnable);
        else runnable.run();
    }

    @EventHandler
    public void onRenderWorld(EventRender3D event) {
        if (mc.world == null) return;

        synchronized (holes) {
            if (holes.isEmpty()) return;
            for (HoleUtils.Hole hole : holes) {
                Box filledBox = new Box(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + fillHeight.getValue().doubleValue(), hole.box().maxZ);
                Box outlinedBox = new Box(hole.box().minX, hole.box().minY, hole.box().minZ, hole.box().maxX, hole.box().minY + outlineHeight.getValue().doubleValue(), hole.box().maxZ);
                if (fill.getValue() == Fill.Normal) Renderer3D.renderBox(event.getMatrices(), filledBox, getFillColor(hole));
                if (fill.getValue() == Fill.Gradient) Renderer3D.renderGradientBox(event.getMatrices(), filledBox, fillHeight.getValue() < 0.0f ? getFillColor(hole) : new Color(0, 0, 0, 0), fillHeight.getValue() < 0.0f ? new Color(0, 0, 0, 0) : getFillColor(hole));
                if (outline.getValue() == Outline.Normal) Renderer3D.renderBoxOutline(event.getMatrices(), outlinedBox, getOutlineColor(hole));
                if (outline.getValue() == Outline.Gradient) Renderer3D.renderGradientBoxOutline(event.getMatrices(), outlinedBox, outlineHeight.getValue() < 0.0f ? getOutlineColor(hole) : new Color(0, 0, 0, 0), outlineHeight.getValue() < 0.0f ? new Color(0, 0, 0, 0) : getOutlineColor(hole));
            }
        }
    }

    private Color getFillColor(HoleUtils.Hole hole) {
        Color color = switch (hole.safety()) {
            case BEDROCK -> bedrockFill.getValue().getColor();
            case MIXED -> mixedFill.getValue().getColor();
            default -> obsidianFill.getValue().getColor();
        };

        return color;
    }

    private Color getOutlineColor(HoleUtils.Hole hole) {
        Color color = switch (hole.safety()) {
            case BEDROCK -> bedrockOutline.getValue().getColor();
            case MIXED -> mixedOutline.getValue().getColor();
            default -> obsidianOutline.getValue().getColor();
        };

        return color;
    }

    @AllArgsConstructor
    private enum Fill implements Nameable {
        None("None"),
        Normal("Normal"),
        Gradient("Gradient");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    private enum Outline implements Nameable {
        None("None"),
        Normal("Normal"),
        Gradient("Gradient");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}