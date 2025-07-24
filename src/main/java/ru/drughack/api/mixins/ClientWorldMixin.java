package ru.drughack.api.mixins;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventEntitySpawn;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void addEntity(Entity entity, CallbackInfo info) {
        DrugHack.getInstance().getEventHandler().post(new EventEntitySpawn(entity));
    }
}