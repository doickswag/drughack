package ru.drughack.managers;

import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import lombok.Getter;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.modules.impl.client.Renders;
import ru.drughack.modules.impl.combat.AutoCrystal;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.Counter;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.RenderPosition;
import ru.drughack.utils.render.Renderer3D;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class RenderManager implements Wrapper {
    private final Counter counter = new Counter();
    private int fps;
    private final CopyOnWriteArrayList<RenderPosition> renderPositions = new CopyOnWriteArrayList<>();
    private Target crystalTarget;

    public RenderManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    @EventHandler
    public void onRenderOverlay(EventRender2D event) {
        fps = counter.getCount();
        counter.increment();
    }

    @EventHandler
    public void onRenderWorld$placePositions(EventRender3D event) {
        if (mc.player == null || mc.world == null || renderPositions.isEmpty()) return;
        Renders module = DrugHack.getInstance().getModuleManager().getRenders();

        for (RenderPosition position : renderPositions) {
            float scale = position.get();
            Box box = new Box(position.getPos());
            if (module.mode.getValue() == Renders.Mode.Fade) box = new Box(position.getPos()).contract(0.5).expand(MathHelper.clamp(scale / 2.0, 0.0, 0.5));
            if (module.renderMode.getValue() == Renders.RenderMode.Fill || module.renderMode.getValue() == Renders.RenderMode.Both) Renderer3D.renderBox(event.getMatrices(), box, ColorUtils.getGlobalColor(50));
            if (module.renderMode.getValue() == Renders.RenderMode.Outline  || module.renderMode.getValue() == Renders.RenderMode.Both) Renderer3D.renderBoxOutline(event.getMatrices(), box, ColorUtils.getGlobalColor());
        }

        renderPositions.removeIf(p -> p.get() <= 0);
    }

    @EventHandler
    public void onRenderWorld$autoCrystal(EventRender3D event) {
        if (mc.player == null || mc.world == null || crystalTarget == null || crystalTarget.getPosition() == null) return;
        AutoCrystal autoCrystalModule = DrugHack.getInstance().getModuleManager().getAutoCrystal();

        float scale;
        if (crystalTarget.getTarget() == 1) scale = 1f;
        else scale = 0f;

        Box box = new Box(crystalTarget.getPosition()).contract(0.5).expand(MathHelper.clamp(scale / 2.0, 0.0, 0.5));

        if (autoCrystalModule.renderMode.getValue() == AutoCrystal.RenderMode.Fill || autoCrystalModule.renderMode.getValue() == AutoCrystal.RenderMode.Both) {
            Color fillColor = ColorUtils.getGlobalColor(50);
            Renderer3D.renderGradientBox(event.getMatrices(), box, fillColor, fillColor);
        }

        if (autoCrystalModule.renderMode.getValue() == AutoCrystal.RenderMode.Outline || autoCrystalModule.renderMode.getValue() == AutoCrystal.RenderMode.Both) {
            Color outlineColor = ColorUtils.getGlobalColor();
            Renderer3D.renderGradientBoxOutline(event.getMatrices(), box, outlineColor, outlineColor);
        }
    }

    @EventHandler
    public void onRenderWorld$autoCrystalExtra(EventRender3D.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (crystalTarget == null || crystalTarget.getPosition() == null) return;
        if (crystalTarget.getTarget() != 1) return;

        MatrixStack matrices = event.getMatrices();
        AutoCrystal module = DrugHack.getInstance().getModuleManager().getAutoCrystal();

        Vec3d vec3d = new Vec3d(crystalTarget.getPosition().toCenterPos().x - mc.getEntityRenderDispatcher().camera.getPos().x, crystalTarget.getPosition().toCenterPos().y - mc.getEntityRenderDispatcher().camera.getPos().y, crystalTarget.getPosition().toCenterPos().z - mc.getEntityRenderDispatcher().camera.getPos().z);

        if (module.renderDamage.getValue()) {
            matrices.push();
            matrices.translate(vec3d.x, vec3d.y, vec3d.z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            matrices.scale(0.025f, -0.025f, 0.025f);
            String text = module.getCalculationDamage();
            DrugHack.getInstance().getFontManager().drawTextWithShadow(matrices, text, -DrugHack.getInstance().getFontManager().getWidth(text) / 2, -DrugHack.getInstance().getFontManager().getHeight() / 2, mc.getBufferBuilders().getEntityVertexConsumers(), Color.WHITE);
            matrices.pop();
        }
    }

    public void setRenderPosition(BlockPos position) {
        if (!DrugHack.getInstance().getModuleManager().getAutoCrystal().isToggled()) position = null;

        if (position == null) {
            if (crystalTarget != null) {
                if (crystalTarget.getTarget() != 0) {
                    crystalTarget.setTarget(0);
                    crystalTarget.setTime(System.currentTimeMillis());
                }
            } else {
                crystalTarget = new Target(null, 0, System.currentTimeMillis());
            }
        } else {
            if (crystalTarget == null || crystalTarget.getTarget() == 0) {
                crystalTarget = new Target(position, 1, System.currentTimeMillis());
            } else {
                crystalTarget.setPosition(position);
            }
        }
    }

    @AllArgsConstructor @Getter @Setter
    public static class Target {
        private BlockPos position;
        private int target;
        private long time;
    }
}