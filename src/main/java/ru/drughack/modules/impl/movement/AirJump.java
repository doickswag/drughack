package ru.drughack.modules.impl.movement;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.api.mixins.accesors.ILivingEntity;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.world.MoveUtils;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

//VEGALINE IS SHIT!!!!!!!!!!!!!!!!!!!!!!
public class AirJump extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Matrix);

    public AirJump() {
        super("AirJump", "jumps for air", Category.Movement);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mode.getValue() == Mode.Matrix) {
            float ex = 1.0f;
            float ex2 = 1.0f;
            if (mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - (double)ex), (int) mc.player.getZ())).getBlock() == Blocks.PURPUR_SLAB
                    || mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - (double)ex), (int) mc.player.getZ())).getBlock() == Blocks.STONE_SLAB) {
                ex += 0.6f;
            }

            ((ILivingEntity) mc.player).setJumpingCooldown(0);

            if ((posBlock(mc.player.getX(), mc.player.getY() - (double)ex, mc.player.getZ())
                    || (posBlock(mc.player.getX() - (double)ex2, mc.player.getY() - (double)ex, mc.player.getZ() - (double)ex2)
                    || posBlock(mc.player.getX() + (double)ex2, mc.player.getY() - (double)ex, mc.player.getZ() + (double)ex2)
                    || posBlock(mc.player.getX() - (double)ex2, mc.player.getY() - (double)ex, mc.player.getZ() + (double)ex2)
                    || posBlock(mc.player.getX() + (double)ex2, mc.player.getY() - (double)ex, mc.player.getZ() - (double)ex2)
                    || posBlock(mc.player.getX() - (double)ex2, mc.player.getY() - (double)ex, mc.player.getZ())
                    || posBlock(mc.player.getX() + (double)ex2, mc.player.getY() - (double)ex, mc.player.getZ())
                    || posBlock(mc.player.getX(), mc.player.getY() - (double)ex, mc.player.getZ() - (double)ex2)
                    || posBlock(mc.player.getX(), mc.player.getY() - (double)ex, mc.player.getZ() + (double)ex2))
                    && MoveUtils.isMoving())
                    && !mc.player.horizontalCollision
                    && mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 0.5), (int) mc.player.getZ())).getBlock() != Blocks.WATER
                    && mc.world.getBlockState(new BlockPos((int) mc.player.getX(), (int) (mc.player.getY() - 1.0), (int) mc.player.getZ())).getBlock() != Blocks.WATER
                    && (!mc.player.verticalCollision || mc.player.age % 2 == 0)) {

                mc.player.setVelocity(new Vec3d(mc.player.getVelocity().x, 0, mc.player.getVelocity().z));
                mc.player.setOnGround(true);
                DrugHack.getInstance().getServerManager().setFallDistance(0);
                if (MoveUtils.getSpeed() < -0.05 && !MoveUtils.isMoving() && mc.options.jumpKey.isPressed()) {
                    mc.player.setVelocity(new Vec3d(
                            mc.player.getVelocity().x - Math.sin(Math.toRadians(mc.player.getYaw())) * MathUtils.clamp(getRandomDouble(-1.0, 1.0), -0.005, 0.005),
                            mc.player.getVelocity().y,
                            mc.player.getVelocity().z + Math.cos(Math.toRadians(mc.player.getYaw())) * MathUtils.clamp(getRandomDouble(-1.0, 1.0), -0.005, 0.005)
                    ));
                }

                mc.player.verticalCollision = mc.player.isOnGround();
                mc.player.setVelocity(new Vec3d(mc.player.getVelocity().x, 0, mc.player.getVelocity().z));
                if (mc.player.getVelocity().y >= 0.0) DrugHack.getInstance().getServerManager().setFallDistance(0);
            }

            if (mc.player.horizontalCollision && (mc.player.getVelocity().y < -0.1 || mc.player.age % 2 == 0)) {
                mc.player.setOnGround(true);
                mc.player.jump();
                DrugHack.getInstance().getServerManager().setFallDistance(0);
            }
        }
    }

    public static boolean posBlock(double x, double y, double z) {
        Block block = mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock();

        if (block == Blocks.WATER || block == Blocks.LAVA) return false;

        if (block instanceof ButtonBlock
                || block instanceof SlabBlock
                || block instanceof AbstractSignBlock
                || block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof TorchBlock
                || block instanceof CarpetBlock
                || block instanceof FlowerBlock
                || block instanceof SaplingBlock
                || block instanceof SkullBlock
                || block instanceof DaylightDetectorBlock
                || block instanceof LilyPadBlock
                || block instanceof BedBlock
                || block instanceof AirBlock) {

            return false;
        }

        return block != Blocks.CAKE
                && block != Blocks.FLOWER_POT
                && block != Blocks.CHORUS_FLOWER
                && block != Blocks.ENCHANTING_TABLE
                && block != Blocks.END_PORTAL_FRAME
                && block != Blocks.DEAD_BUSH
                && block != Blocks.REDSTONE_WIRE
                && block != Blocks.SNOW;
    }

    public double getRandomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max + 1.0);
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName();
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Matrix("Matrix");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}