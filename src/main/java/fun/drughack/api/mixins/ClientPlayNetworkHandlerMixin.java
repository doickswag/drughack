package fun.drughack.api.mixins;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.drughack.DrugHack;
import fun.drughack.utils.network.ChatUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(@NotNull String message, CallbackInfo ci) {
        if (message.startsWith(DrugHack.getInstance().getCommandManager().getPrefix()) && !DrugHack.getInstance().isPanic()) {
            try {
                DrugHack.getInstance().getCommandManager().getDispatcher().execute(message.substring(DrugHack.getInstance().getCommandManager().getPrefix().length()), DrugHack.getInstance().getCommandManager().getSource());
            } catch (CommandSyntaxException e) {
                ChatUtils.sendMessage(Formatting.RED + e.getMessage());
            }

            ci.cancel();
        }
    }
}