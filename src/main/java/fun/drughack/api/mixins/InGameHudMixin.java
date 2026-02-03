package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.modules.impl.misc.ItemHelper;
import fun.drughack.modules.impl.render.NoRender;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    public void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventRender2D event = new EventRender2D(context, tickCounter);
        DrugHack.getInstance().getEventHandler().post(event);
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    public void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (DrugHack.getInstance().getModuleManager().getModule(NoRender.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoRender.class).potions.getValue()) ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    public void renderScoreboardSidebar(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        if (DrugHack.getInstance().getModuleManager().getModule(NoRender.class).isToggled() && DrugHack.getInstance().getModuleManager().getModule(NoRender.class).scoreboard.getValue()) ci.cancel();
    }

    @Inject(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;III)V", shift = At.Shift.AFTER))
    public void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (DrugHack.getInstance().getModuleManager().getModule(ItemHelper.class).isToggled()) {
            if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                Render2D.drawRoundedRect(
                        context.getMatrices(),
                        x,
                        y,
                        16f,
                        16f,
                        0,
                        ColorUtils.pulse(new Color(255, 255, 50, 180), 30)
                );
            } else if (stack.getItem() == Items.GOLDEN_APPLE) {
                Render2D.drawRoundedRect(
                        context.getMatrices(),
                        x,
                        y,
                        16f,
                        16f,
                        0,
                        ColorUtils.pulse(new Color(255, 255, 255, 180), 30)
                );
            } else if (isPotion(stack, StatusEffects.INSTANT_HEALTH.value())) {
                Render2D.drawRoundedRect(
                        context.getMatrices(),
                        x,
                        y,
                        16f,
                        16f,
                        0,
                        ColorUtils.pulse(new Color(255, 50, 50, 180), 30)
                );
            }
        }
    }

    @Unique
    private boolean isPotion(ItemStack stack, StatusEffect status) {
        if (!(stack.getItem() instanceof PotionItem)) return false;
        PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) return false;
        if (component.potion().isEmpty()) return false;
        for (StatusEffectInstance effect : component.potion().get().value().getEffects()) if (effect.getEffectType().value() == status) return true;

        return false;
    }
}