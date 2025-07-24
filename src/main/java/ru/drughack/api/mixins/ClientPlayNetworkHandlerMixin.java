package ru.drughack.api.mixins;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventChatSend;
import ru.drughack.api.event.impl.EventClientConnect;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Unique private EventChatSend event;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        DrugHack.getInstance().getEventHandler().post(new EventClientConnect());
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(@NotNull String message, CallbackInfo ci) {
        if (message.startsWith(DrugHack.getInstance().getCommandManager().getPrefix())) {
            try {
                DrugHack.getInstance().getCommandManager().getDispatcher().execute(message.substring(DrugHack.getInstance().getCommandManager().getPrefix().length()), DrugHack.getInstance().getCommandManager().getSource());
            } catch (CommandSyntaxException ex) {
                ex.printStackTrace();
            }
            ci.cancel();
        }
    }

    @ModifyVariable(method = "sendChatMessage", at=@At(value="INVOKE", target="Ljava/time/Instant;now()Ljava/time/Instant;", shift = At.Shift.BEFORE), argsOnly = true)
    private String shit(String message) {
        event = new EventChatSend(message);
        DrugHack.getInstance().getEventHandler().post(event);
        return event.getMessage();
    }

    @Inject(method = "sendChatMessage", at = @At(value="INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;", shift=At.Shift.AFTER), cancellable = true)
    private void shit(String shit, CallbackInfo ci) {
        if (event != null && event.isCanceled()) ci.cancel();
    }
}