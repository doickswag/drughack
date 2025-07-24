package ru.drughack.utils.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Setter
@Getter
public class RenderPosition {
    private BlockPos pos;
    private long startTime;

    public RenderPosition(BlockPos pos) {
        this.pos = pos;
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RenderPosition) return ((RenderPosition) o).pos.equals(this.pos);
        return false;
    }

    public float get() {
        return 1.0f - toDelta$(startTime);
    }

    private float toDelta$(long start) {
        return MathHelper.clamp(toDelta(start) / 75f, 0.0f, 1.0f);
    }

    private long toDelta(long start) {
        return System.currentTimeMillis() - start;
    }
}