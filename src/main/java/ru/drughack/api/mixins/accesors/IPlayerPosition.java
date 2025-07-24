package ru.drughack.api.mixins.accesors;

import net.minecraft.entity.player.PlayerPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPosition.class)
public interface IPlayerPosition {
    @Accessor("yaw") @Mutable
    void setYaw(float yaw);

    @Accessor("pitch") @Mutable
    void setPitch(float pitch);
}
