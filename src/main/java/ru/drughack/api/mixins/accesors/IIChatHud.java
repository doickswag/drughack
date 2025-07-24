package ru.drughack.api.mixins.accesors;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatHud.class)
public interface IIChatHud {
    @Accessor("messages")
    List<ChatHudLine> getMessages();

    @Accessor("visibleMessages")
    List<ChatHudLine.Visible> getVisibleMessages();

    @Accessor("scrolledLines")
    int getScrolledLines();

    @Accessor("hasUnreadNewMessages")
    void setHasUnreadNewMessages(boolean hasUnreadNewMessages);

    @Invoker("logChatMessage")
    void invokeLogChatMessage(ChatHudLine message);

    @Invoker("addVisibleMessage")
    void invokeAddVisibleMessage(ChatHudLine message);

    @Invoker("addMessage")
    void invokeAddMessage(ChatHudLine message);
}