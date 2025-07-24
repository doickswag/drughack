package ru.drughack.api.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.utils.formatting.CustomFormatting;

import static ru.drughack.utils.interfaces.Wrapper.mc;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Unique private final String text = CustomFormatting.CLIENT + DrugHack.getInstance().getProtection().getName() + " " + Formatting.WHITE + DrugHack.getInstance().getProtection().getVersion();
    @Unique private final String commitText = (" (build at %s)"
            .formatted(DrugHack.getInstance().getProtection().getBuildTime()));

    @Inject(method = "render", at = @At("TAIL"))
    private void renderHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.drawText(mc.textRenderer, text + Formatting.GRAY + commitText, 2, 2, -1, true);
    }
}