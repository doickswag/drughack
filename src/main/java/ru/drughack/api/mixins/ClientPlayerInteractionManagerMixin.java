package ru.drughack.api.mixins;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventBreakBlock;
import ru.drughack.api.event.impl.EventAttackBlock;
import ru.drughack.api.event.impl.EventAttackEntity;
import ru.drughack.api.event.impl.EventClickSlot;
import ru.drughack.modules.api.Module;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;
        EventClickSlot event = new EventClickSlot(actionType, slotId, button, syncId);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        EventAttackBlock event = new EventAttackBlock(pos, direction);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventAttackEntity event = new EventAttackEntity(player, target);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        DrugHack.getInstance().getEventHandler().post(new EventBreakBlock(pos));
    }
}