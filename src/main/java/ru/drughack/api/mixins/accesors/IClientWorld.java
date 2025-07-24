package ru.drughack.api.mixins.accesors;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientWorld.class)
public interface IClientWorld {

    @Invoker("getPendingUpdateManager")
    PendingUpdateManager invokeGetPendingUpdateManager();
}