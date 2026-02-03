package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.modules.impl.render.NameTags;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity>  {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    public void getDisplayName(T entity, CallbackInfoReturnable<Text> cir) {
        if (DrugHack.getInstance().getModuleManager().getModule(NameTags.class).isToggled() && entity instanceof PlayerEntity) cir.setReturnValue(null);
    }
}