package ru.drughack.api.mixins;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventCameraRotate;


@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private boolean thirdPerson;

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
    private void update(Args args) {
        if (DrugHack.getInstance().getModuleManager().getViewClip().isToggled() && DrugHack.getInstance().getModuleManager().getViewClip().extend.getValue()) {
            args.set(0, DrugHack.getInstance().getModuleManager().getViewClip().distance.getValue().floatValue());
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    public void update2(Args args) {
        EventCameraRotate event = new EventCameraRotate(args.get(0), args.get(1));
        DrugHack.getInstance().getEventHandler().post(event);

        args.set(0, event.getYaw());
        args.set(1, event.getPitch());
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void clipToSpace(float f, CallbackInfoReturnable<Float> info) {
        if (DrugHack.getInstance().getModuleManager().getViewClip().isToggled()) {
            info.setReturnValue(f);
        }
    }

    @Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
    private void getSubmersionType(CallbackInfoReturnable<CameraSubmersionType> info) {
        if (DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().liquidOverlay.getValue()) {
            info.setReturnValue(CameraSubmersionType.NONE);
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void update3(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (DrugHack.getInstance().getModuleManager().getFreecam().isToggled()) {
            this.thirdPerson = true;
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void update4(Args args) {
        if (DrugHack.getInstance().getModuleManager().getFreecam().isToggled()) {
            args.setAll(DrugHack.getInstance().getModuleManager().getFreecam().getFreeYaw(), DrugHack.getInstance().getModuleManager().getFreecam().getFreePitch());
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void update5(Args args) {
        if (DrugHack.getInstance().getModuleManager().getFreecam().isToggled()) {
            args.setAll(DrugHack.getInstance().getModuleManager().getFreecam().getFreeX(), DrugHack.getInstance().getModuleManager().getFreecam().getFreeY(), DrugHack.getInstance().getModuleManager().getFreecam().getFreeZ());
        }
    }
}