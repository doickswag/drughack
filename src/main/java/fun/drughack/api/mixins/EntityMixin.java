package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.rotations.EventTrace;
import fun.drughack.api.events.impl.rotations.EventTravel;
import fun.drughack.modules.impl.misc.NoPush;
import fun.drughack.utils.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements Wrapper {

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void pushAwayFrom(CallbackInfo ci) {
        if ((Object) this == mc.player && DrugHack.getInstance().getModuleManager().getModule(NoPush.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoPush.class).players.getValue()) ci.cancel();
    }

    @Inject(method = "isPushedByFluids", at = @At("RETURN"), cancellable = true)
    public void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == mc.player && DrugHack.getInstance().getModuleManager().getModule(NoPush.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoPush.class).water.getValue()) cir.setReturnValue(false);
    }

    @Redirect(method = "raycast", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d raycast(Entity instance, float tickDelta) {
        if (instance == mc.player) {
            EventTrace event = new EventTrace(instance.getYaw(), instance.getPitch());
            DrugHack.getInstance().getEventHandler().post(event);
            if (event.isCancelled()) return instance.getRotationVector(instance.getPitch(), instance.getYaw());
        }

        return instance.getRotationVec(tickDelta);
    }

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw()F"))
    public float updateVelocity(Entity instance) {
        if (instance == mc.player) {
            EventTravel event = new EventTravel(instance.getYaw(), instance.getPitch());
            DrugHack.getInstance().getEventHandler().post(event);
            return event.getYaw();
        }
        
        return instance.getYaw();
    }
}