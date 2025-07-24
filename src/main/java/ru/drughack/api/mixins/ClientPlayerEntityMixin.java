package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.*;
import ru.drughack.modules.impl.movement.NoSlow;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract void autoJump(float dx, float dz);

    @Shadow public abstract float getYaw(float tickDelta);

    @Shadow public abstract float getPitch(float tickDelta);

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void move(MovementType movementType, Vec3d movement, CallbackInfo info) {
        EventMotion event = new EventMotion(movementType, movement);
        DrugHack.getInstance().getEventHandler().post(event);

        if (event.isCanceled()) {
            info.cancel();
            double prevX = getX();
            double prevZ = getZ();
            super.move(movementType, event.getMovement());
            autoJump((float) (getX() - prevX), (float) (getZ() - prevZ));
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.AFTER))
    private void tick$AFTER(CallbackInfo info) {
        DrugHack.getInstance().getEventHandler().post(new EventMoveUpdate());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.BEFORE))
    private void tick$BEFORE(CallbackInfo info) {
        DrugHack.getInstance().getEventHandler().post(new EventPlayerUpdate());
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;tickables:Ljava/util/List;", shift = At.Shift.BEFORE))
    private void tick$tickables(CallbackInfo ci) {
        DrugHack.getInstance().getEventHandler().post(new EventMoveUpdate.Post());
    }

    @Inject(method = "setCurrentHand", at = @At(value = "HEAD"))
    private void setCurrentHand(Hand hand, CallbackInfo info) {
        DrugHack.getInstance().getEventHandler().post(new EventChangeHand());
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void pushOutOfBlocks(double x, double z, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getVelocity().isToggled() && DrugHack.getInstance().getModuleManager().getVelocity().antiBlockPush.getValue()) info.cancel();
    }


    @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean canStartSprinting$isUsingItem(boolean original) {
        if (DrugHack.getInstance().getModuleManager().getNoSlow().isToggled() && !DrugHack.getInstance().getModuleManager().getNoSlow().shouldSlow()) return false;
        return original;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean tickMovement$isUsingItem(boolean original) {
        if (DrugHack.getInstance().getModuleManager().getNoSlow().isToggled() && !DrugHack.getInstance().getModuleManager().getNoSlow().shouldSlow()) return false;
        return original;
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo ci) {
        DrugHack.getInstance().getEventHandler().post(new EventSync(getYaw(), getPitch()));
    }
}