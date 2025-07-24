package ru.drughack.modules.impl.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventPacketReceive;
import ru.drughack.api.event.impl.EventRender3D;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.utils.render.Renderer3D;
import ru.drughack.utils.world.BlockUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BreakHighlight extends Module {

    public Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new ColorSetting.Colors(new Color(255, 0, 0, 50), false)));
    public Setting<ColorSetting> outlineColor = new Setting<>("Outline Color", new ColorSetting(new ColorSetting.Colors(new Color(255, 0, 0, 255), false)));

    public BreakHighlight() {
        super("BreakHighlight", "shows break progress of targets", Category.Render);
    }

    private final Map<BlockBreakingProgressS2CPacket, Long> breakingProgress = new ConcurrentHashMap<>();

    @EventHandler
    public void onPacketReceive(EventPacketReceive e) {
        if (e.getPacket() instanceof BlockBreakingProgressS2CPacket packet && !BlockUtils.isUnbreakable(packet.getPos())) {
            BlockBreakingProgressS2CPacket pos = getPacketFromPos(packet.getPos());
            if (pos != null) breakingProgress.replace(pos, System.currentTimeMillis());
            else breakingProgress.put(packet, System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (fullNullCheck()) return;

        for (Map.Entry<BlockBreakingProgressS2CPacket, Long> mine : breakingProgress.entrySet()) {
            BlockPos mining = mine.getKey().getPos();
            long elapsedTime = System.currentTimeMillis() - mine.getValue();
            long count = breakingProgress.keySet().stream().filter(p -> p.getEntityId() == mine.getKey().getEntityId()).count();

            while (count > 2) {
                breakingProgress.entrySet().stream().filter(p -> p.getKey().getEntityId() == mine.getKey().getEntityId()).min(Comparator.comparingLong(Map.Entry::getValue)).ifPresent(min -> breakingProgress.remove(min.getKey(), min.getValue()));
                count--;
            }

            if (mc.world.isAir(mining) || elapsedTime > 2500) {
                breakingProgress.remove(mine.getKey(), mine.getValue());
                continue;
            }

            VoxelShape outlineShape = mc.world.getBlockState(mining).getOutlineShape(mc.world, mining);
            outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            Box bounding = outlineShape.getBoundingBox();
            Box render = new Box(
                    mining.getX() + bounding.minX,
                    mining.getY() + bounding.minY,
                    mining.getZ() + bounding.minZ,
                    mining.getX() + bounding.maxX,
                    mining.getY() + bounding.maxY,
                    mining.getZ() + bounding.maxZ
            );
            Vec3d center = render.getCenter();
            float scale = MathHelper.clamp(elapsedTime / 2500.0f, 0.0f, 1.0f);
            double dx = (bounding.maxX - bounding.minX) / 2.0;
            double dy = (bounding.maxY - bounding.minY) / 2.0;
            double dz = (bounding.maxZ - bounding.minZ) / 2.0;
            Box box = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
            Renderer3D.renderBox(e.getMatrices(), box, fillColor.getValue().getColor());
            Renderer3D.renderBoxOutline(e.getMatrices(), box, outlineColor.getValue().getColor());
        }
    }

    private BlockBreakingProgressS2CPacket getPacketFromPos(BlockPos pos) {
        return breakingProgress.keySet().stream().filter(p -> p.getPos().equals(pos)).findFirst().orElse(null);
    }
}