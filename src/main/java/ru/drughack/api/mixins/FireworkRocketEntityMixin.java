package ru.drughack.api.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventFireworkVector;
import ru.drughack.api.event.impl.EventFireworkVelocity;

import static ru.drughack.utils.interfaces.Wrapper.mc;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @Shadow private LivingEntity shooter;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d tick(LivingEntity instance) {
        if (shooter == mc.player) {
            EventFireworkVector event = new EventFireworkVector(shooter.getRotationVector());
            DrugHack.getInstance().getEventHandler().post(event);
            if (event.isCanceled()) return event.getVector();
            else return shooter.getRotationVector();
        }

        return shooter.getRotationVector();
    }

    @ModifyArgs(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    public void tick2(Args args) {
        if (shooter == mc.player) {
            EventFireworkVelocity event = new EventFireworkVelocity(shooter.getVelocity());
            DrugHack.getInstance().getEventHandler().post(event);
            if (event.isCanceled()) args.set(0, event.getVelocity());
        }
    }
}