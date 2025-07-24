package ru.drughack.api.mixins;

import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.drughack.utils.mixins.IChatHudLine;

@Mixin(ChatHudLine.class)
public abstract class ChatHudLineMixin implements IChatHudLine {
    @Unique private boolean clientMessage = false;
    @Unique private String clientIdentifier = "";

    @Override
    public boolean drughack$isClientMessage() {
        return clientMessage;
    }

    @Override
    public void drughack$setClientMessage(boolean clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public String drughack$getClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public void drughack$setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}