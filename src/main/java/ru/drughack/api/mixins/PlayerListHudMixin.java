package ru.drughack.api.mixins;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;
import ru.drughack.utils.formatting.CustomFormatting;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        boolean irc = DrugHack.getInstance().getModuleManager().getIrc().isToggled();
        boolean isTHUser = DrugHack.getInstance().getGvobavs().isTHUser(entry.getProfile().getName());
        boolean isDrugUser = DrugHack.getInstance().getGvobavs().isUser(entry.getProfile().getName());
        String zzz = Formatting.strip(cir.getReturnValue().getString());
        if (irc && isTHUser) cir.setReturnValue(Text.of( CustomFormatting.CLIENT + "[thunder] " + zzz));
        if (irc && isDrugUser) cir.setReturnValue(Text.of( CustomFormatting.CLIENT + "[drug] " + zzz));
    }
}