package ru.drughack.api.mixins;

import net.minecraft.client.font.Glyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.modules.impl.client.FontModule;

@Mixin(Glyph.class)
public interface GlyphMixin {

    @Inject(method = "getShadowOffset", at = @At("HEAD"), cancellable = true)
    private void getShadowOffset(CallbackInfoReturnable<Float> cir) {
        boolean textShadow = DrugHack.getInstance().getModuleManager().getNoRender().isToggled() && DrugHack.getInstance().getModuleManager().getNoRender().textShadow.getValue();

        if (!textShadow && !(DrugHack.getInstance().getModuleManager().getFontModule().shadowMode.getValue() == FontModule.Shadow.Default)) cir.setReturnValue(DrugHack.getInstance().getFontManager().getShadowOffset());
        else if (textShadow) cir.setReturnValue(0.6f);
        else cir.setReturnValue(1f);
    }
}