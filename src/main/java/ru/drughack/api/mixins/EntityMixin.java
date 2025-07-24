package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventPlayerChangeLook;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.api.event.impl.EventUpdateVelocity;

@Mixin(Entity.class)
public abstract class EntityMixin implements Wrapper {

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void updateVelocity(float speed, Vec3d movementInput, CallbackInfo info) {
        if ((Object) this != mc.player) return;

        EventUpdateVelocity event = new EventUpdateVelocity(movementInput, speed);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) {
            info.cancel();
            mc.player.setVelocity(mc.player.getVelocity().add(event.getVelocity()));
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity entity, CallbackInfo info) {
        if ((Object) this == mc.player && DrugHack.getInstance().getModuleManager().getVelocity().isToggled() && DrugHack.getInstance().getModuleManager().getVelocity().antiPush.getValue()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getVelocity()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d updateMovementInFluid(Vec3d vec3d) {
        if ((Object) this == mc.player && DrugHack.getInstance().getModuleManager().getVelocity().isToggled() && DrugHack.getInstance().getModuleManager().getVelocity().antiLiquidPush.getValue()) {
            return new Vec3d(0, 0, 0);
        }

        return vec3d;
    }


    @ModifyExpressionValue(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block getVelocityMultiplier(Block original) {
        if (DrugHack.getInstance().getModuleManager().getNoSlow().isToggled()) {
            if ((original == Blocks.SOUL_SAND && DrugHack.getInstance().getModuleManager().getNoSlow().soulSand.getValue()) || (original == Blocks.HONEY_BLOCK && DrugHack.getInstance().getModuleManager().getNoSlow().honeyBlocks.getValue())) {
                return Blocks.STONE;
            }
        }

        return original;
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void changeLookDirection$PRE(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        EventPlayerChangeLook event = new EventPlayerChangeLook(cursorDeltaX, cursorDeltaY);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "changeLookDirection", at = @At("RETURN"), cancellable = true)
    public void changeLookDirection$POST(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        EventPlayerChangeLook.Post event = new EventPlayerChangeLook.Post(cursorDeltaX, cursorDeltaY);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) ci.cancel();
    }
}