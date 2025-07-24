package ru.drughack.modules.impl.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.render.Renderer2D;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.world.BlockUtils;

import java.awt.*;

public class BedTags extends Module {

    public Setting<Float> range = new Setting<>("Range", 50f, 10f, 100f);

    public BedTags() {
        super("BedTags", "shown the bed on bedwars", Category.Render);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        MatrixStack matrices = e.getMatrices();
        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();

        for (BlockEntity entity : BlockUtils.getLoadedBlockEntitiesOnArrayList()) {
            if (!(entity instanceof BedBlockEntity bed)) continue;
            double distance = mc.player.getPos().distanceTo(Vec3d.ofCenter(bed.getPos()));
            if (distance > range.getValue()) continue;

            String name = mc.world.getBlockState(bed.getPos()).getBlock().getName().getString();
            name += " " + String.format("%.1f", distance) + "m";

            int originalColor = bed.getColor().getEntityColor();
            Color color = new Color(
                    (originalColor >> 16) & 0xFF,
                    (originalColor >> 8) & 0xFF,
                    originalColor & 0xFF,
                    50
            );

            Box box = BlockUtils.getBoundingBox(entity.getPos());
            Renderer3D.renderBox(e.getMatrices(), box, color);
            Renderer3D.renderBoxOutline(e.getMatrices(), box, new Color(bed.getColor().getEntityColor()));

            Vec3d blockPos = new Vec3d(
                    entity.getPos().getX() + 0.5,
                    entity.getPos().getY() + 1.2,
                    entity.getPos().getZ() + 0.5
            );

            Vec3d cameraPos = mc.getEntityRenderDispatcher().camera.getPos();
            Vec3d relativePos = blockPos.subtract(cameraPos);
            float scaling = 0.0018f + (30f / 10000.0f) * (float)distance;
            if (distance <= 8.0) scaling = 0.0245f;

            matrices.push();
            matrices.translate(relativePos.x, relativePos.y, relativePos.z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            matrices.scale(scaling, -scaling, scaling);

            int width = DrugHack.getInstance().getFontManager().getWidth(name);

            // Рендерим фон и текст
            Renderer2D.renderQuad(matrices, -width / 2.0f - 1, -DrugHack.getInstance().getFontManager().getHeight() - 1,
                    width / 2.0f + 2, 0, new Color(0, 0, 0, 100));

            DrugHack.getInstance().getFontManager().drawTextWithShadow(
                    matrices,
                    name,
                    -width / 2,
                    -DrugHack.getInstance().getFontManager().getHeight(),
                    vertexConsumers,
                    Color.WHITE
            );

            matrices.pop();
        }
    }
}