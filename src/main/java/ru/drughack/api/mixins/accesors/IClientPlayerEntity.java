package ru.drughack.api.mixins.accesors;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface IClientPlayerEntity {
    @Invoker("isWalking")
    boolean invokeIsWalking();

    @Invoker("canSprint")
    boolean invokeCanSprint();

    @Accessor("lastOnGround")
    void setLastOnGround(boolean lastOnGround);
}