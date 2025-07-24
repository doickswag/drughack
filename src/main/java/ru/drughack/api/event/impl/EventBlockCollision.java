package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import ru.drughack.api.event.Event;

import static ru.drughack.utils.interfaces.Wrapper.mc;

@AllArgsConstructor @Getter
public class EventBlockCollision extends Event {
    private BlockPos blockPos;
    private VoxelShape state;

    public void setState(BlockState state) {
        this.state = state.getCollisionShape(mc.world, blockPos);
    }
}