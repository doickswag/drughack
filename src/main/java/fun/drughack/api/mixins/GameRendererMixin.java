package fun.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender3D;
import fun.drughack.api.events.impl.rotations.EventTrace;
import fun.drughack.modules.impl.combat.NoEntityTrace;
import fun.drughack.modules.impl.combat.NoFriendDamage;
import fun.drughack.modules.impl.render.NoRender;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.render.Render3D;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements Wrapper {

    @Inject(method = "renderWorld", at = @At("HEAD"))
    public void renderWorld(RenderTickCounter renderTickCounter, CallbackInfo ci) {
        Render3D.prepare();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    public void renderWorld(RenderTickCounter renderTickCounter, CallbackInfo info, @Local(ordinal = 2) Matrix4f matrix4f3, @Local(ordinal = 1) float tickDelta, @Local MatrixStack matrixStack) {
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(matrix4f3);
        MatrixStack cleanMatrixStack = new MatrixStack();
        RenderSystem.getModelViewStack().mul(cleanMatrixStack.peek().getPositionMatrix());
        EventRender3D.Game event = new EventRender3D.Game(renderTickCounter, matrixStack);
        DrugHack.getInstance().getEventHandler().post(event);
        Render3D.draw(Render3D.QUADS, Render3D.DEBUG_LINES, false);
        Render3D.draw(Render3D.SHINE_QUADS, Render3D.SHINE_DEBUG_LINES, true);
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    public void showFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
        if (DrugHack.getInstance().getModuleManager().getModule(NoRender.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoRender.class).totem.getValue()) ci.cancel();
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    public void tiltViewWhenHurt(CallbackInfo ci) {
        if (DrugHack.getInstance().getModuleManager().getModule(NoRender.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoRender.class).hurtCam.getValue()) ci.cancel();
    }

    @ModifyExpressionValue(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    public EntityHitResult findCrosshairTarget(EntityHitResult original) {
        if (original != null
                && DrugHack.getInstance().getFriendManager().isFriend(original.getEntity().getName().getString())
                && DrugHack.getInstance().getModuleManager().getModule(NoFriendDamage.class).isToggled()) return null;
        else if (original != null
                && DrugHack.getInstance().getModuleManager().getModule(NoEntityTrace.class).isToggled()) return null;
        else return original;
    }

    @Redirect(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d findCrosshairTarget(Entity instance, float tickDelta) {
        if (instance == mc.player) {
            EventTrace event = new EventTrace(instance.getYaw(), instance.getPitch());
            DrugHack.getInstance().getEventHandler().post(event);
            if (event.isCancelled()) return instance.getRotationVector(event.getPitch(), event.getYaw());
        }

        return instance.getRotationVec(tickDelta);
    }
}