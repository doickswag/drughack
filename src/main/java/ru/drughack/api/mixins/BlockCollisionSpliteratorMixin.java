package ru.drughack.api.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventBlockCollision;
import ru.drughack.modules.api.Module;

@Mixin(BlockCollisionSpliterator.class)
public abstract class BlockCollisionSpliteratorMixin {

    @Redirect(
            method = "computeNext",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"
            )
    )
    private VoxelShape computeNext(ShapeContext instance, BlockState blockState, CollisionView collisionView, BlockPos blockPos) {
        if (!Module.fullNullCheck()) {
            EventBlockCollision event = new EventBlockCollision(blockPos, instance.getCollisionShape(blockState, collisionView, blockPos));
            DrugHack.getInstance().getEventHandler().post(event);
            return event.getState();
        } else return instance.getCollisionShape(blockState, collisionView, blockPos);
    }
}