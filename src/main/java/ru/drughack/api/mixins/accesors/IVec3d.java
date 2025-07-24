package ru.drughack.api.mixins.accesors;

import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3d.class)
public interface IVec3d {
    @Accessor("x") @Mutable
    void setX(double x);

    @Accessor("y") @Mutable
    void setY(double y);

    @Accessor("z") @Mutable
    void setZ(double z);
}