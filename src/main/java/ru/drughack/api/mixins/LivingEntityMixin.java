package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventConsumeItem;
import ru.drughack.api.event.impl.EventJumpPlayer;
import ru.drughack.api.event.impl.EventPlayerTravel;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.impl.movement.HighJump;
import ru.drughack.utils.mixins.IILivingEntity;

import static ru.drughack.utils.interfaces.Wrapper.mc;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IILivingEntity {

    @Shadow @Final private static EntityAttributeModifier SPRINTING_SPEED_BOOST;
    @Shadow public abstract @Nullable EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute);
    @Shadow protected ItemStack activeItemStack;
    @Unique private boolean staticPlayerEntity = false;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public boolean drughack$isStaticPlayerEntity() {
        return staticPlayerEntity;
    }

    @Override
    public void drughack$setStaticPlayerEntity(boolean staticPlayerEntity) {
        this.staticPlayerEntity = staticPlayerEntity;
    }

    @WrapMethod(method = "getStepHeight")
    public float getOffGroundSpeed(Operation<Float> original) {
        if (!Module.fullNullCheck() && DrugHack.getInstance().getModuleManager().getHighJump().isToggled() && DrugHack.getInstance().getModuleManager().getHighJump().mode.getValue() == HighJump.Mode.Funtime) return 0.035f;
        else return original.call();
    }

    @WrapMethod(method = "getStepHeight")
    private float getStepHeight(Operation<Float> original) {
        if ((Object) this == mc.player && DrugHack.getInstance().getModuleManager() != null && ((DrugHack.getInstance().getModuleManager().getStep().isToggled() && mc.player.isOnGround()))) {
            return DrugHack.getInstance().getModuleManager().getStep().height.getValue().floatValue();
        }

        return original.call();
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void jump$HEAD(CallbackInfo info) {
        if ((Object) this != mc.player) return;
        DrugHack.getInstance().getEventHandler().post(new EventJumpPlayer());
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void jump$RETURN(CallbackInfo info) {
        if ((Object) this != mc.player) return;
        DrugHack.getInstance().getEventHandler().post(new EventJumpPlayer.Post());
    }

    @Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void consumeItem(CallbackInfo ci) {
        if ((Object) this == mc.player) DrugHack.getInstance().getEventHandler().post(new EventConsumeItem(activeItemStack));
    }

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void getHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
        int speed = (int) (6 / DrugHack.getInstance().getModuleManager().getSwing().speed.getValue());
        if (DrugHack.getInstance().getModuleManager().getSwing().isToggled()) {
            cir.cancel();
            if (StatusEffectUtil.hasHaste(mc.player)) cir.setReturnValue(speed - (1 + StatusEffectUtil.getHasteAmplifier(mc.player)));
            else cir.setReturnValue(mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) ? speed + (1 + mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : speed);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(CallbackInfo ci) {
        if ((Object) this != mc.player) return;
        EventPlayerTravel event = new EventPlayerTravel();
        DrugHack.getInstance().getEventHandler().post(event);

        if (event.isCanceled()) {
            move(MovementType.SELF, getVelocity());
            ci.cancel();
        }
    }
}