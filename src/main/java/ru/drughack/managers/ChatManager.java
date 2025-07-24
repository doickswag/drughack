package ru.drughack.managers;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IIChatHud;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.mixins.IChatHudLine;
import ru.drughack.utils.mixins.IChatHudLineVisible;

import java.util.ArrayList;
import java.util.List;

public class ChatManager implements Wrapper {
    private final List<String> awaitMessages = new ArrayList<>();

    public ChatManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.inGameHud == null) return;
        if (awaitMessages.isEmpty()) return;

        for (String message : new ArrayList<>(awaitMessages)) {
            addMessage(message);
            awaitMessages.remove(message);
        }
    }

    public void message(String message) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getPrefix() + " " + Formatting.WHITE + message);
    }

    public void message(String prefix, String message) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getPrefix(prefix) + " " + Formatting.WHITE + message);
    }

    public void added(String message, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getPrefix() + Formatting.DARK_GREEN + " [" + Formatting.GREEN + "+" + Formatting.DARK_GREEN + "] " + Formatting.WHITE + message, identifier);
    }

    public void deleted(String message, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getPrefix() + Formatting.DARK_RED + " [" + Formatting.RED + "-" + Formatting.DARK_RED + "] " + Formatting.WHITE + message, identifier);
    }

    public void success(String message, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getPrefix() + Formatting.DARK_GREEN + " [" + Formatting.GREEN + "!" + Formatting.DARK_GREEN + "] " + Formatting.WHITE + message, identifier);
    }
    
    public void warn(String message, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getPrefix() + Formatting.GOLD + " [" + Formatting.YELLOW + "!" + Formatting.GOLD + "] " + Formatting.WHITE + message, identifier);
    }

    public void error(String message, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getPrefix() + Formatting.DARK_RED + " [" + Formatting.RED + "!" + Formatting.DARK_RED + "] " + Formatting.WHITE + message, identifier);
    }

    public void await(String message) {
        awaitMessages.add(getPrefix() + " " + Formatting.WHITE + message);
    }

    public void addMessage(String message) {
        addTextMessage(Text.literal(message), "");
    }

    public void addMessage(String message, String identifier) {
        addTextMessage(Text.literal(message), identifier);
    }

    private void addTextMessage(Text message, String identifier) {
        ChatHudLine line = new ChatHudLine(mc.inGameHud.getTicks(), message, null, MessageIndicator.system());
        ((IChatHudLine) (Object) line).drughack$setClientMessage(true);
        ((IChatHudLine) (Object) line).drughack$setClientIdentifier(identifier);
        ((IIChatHud) mc.inGameHud.getChatHud()).invokeLogChatMessage(line);
        ((IIChatHud) mc.inGameHud.getChatHud()).invokeAddMessage(line);

        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(line.content(), MathHelper.floor((double) mc.inGameHud.getChatHud().getWidth() / mc.inGameHud.getChatHud().getChatScale()), mc.textRenderer);
        for (int i = 0; i < list.size(); ++i) {
            OrderedText orderedText = list.get(i);

            if (mc.inGameHud.getChatHud().isChatFocused() && ((IIChatHud) mc.inGameHud.getChatHud()).getScrolledLines() > 0) {
                ((IIChatHud) mc.inGameHud.getChatHud()).setHasUnreadNewMessages(true);
                mc.inGameHud.getChatHud().scroll(1);
            }

            boolean bool = i == list.size() - 1;
            ChatHudLine.Visible visible = new ChatHudLine.Visible(line.creationTick(), orderedText, line.indicator(), bool);
            ((IChatHudLineVisible) (Object) visible).drughack$setClientMessage(true);
            ((IChatHudLineVisible) (Object) visible).drughack$setClientIdentifier(identifier);
            ((IIChatHud) mc.inGameHud.getChatHud()).getVisibleMessages().addFirst(visible);
        }

        while (((IIChatHud) mc.inGameHud.getChatHud()).getVisibleMessages().size() > 100) ((IIChatHud) mc.inGameHud.getChatHud()).getVisibleMessages().removeLast();
    }

    public static void deleteMessage(String identifier) {
        try {
            ArrayList<ChatHudLine> removedLines = new ArrayList<>();
            for (ChatHudLine message : ((IIChatHud) mc.inGameHud.getChatHud()).getMessages()) {
                if (!((IChatHudLine) (Object) message).drughack$isClientMessage() || ((IChatHudLine) (Object) message).drughack$getClientIdentifier().isEmpty()) continue;
                if (((IChatHudLine) (Object) message).drughack$getClientIdentifier().equals(identifier)) removedLines.add(message);
            }

            ArrayList<ChatHudLine.Visible> removedVisibleLines = new ArrayList<>();
            for (ChatHudLine.Visible message : ((IIChatHud) mc.inGameHud.getChatHud()).getVisibleMessages()) {
                if (!((IChatHudLineVisible) (Object) message).drughack$isClientMessage() || ((IChatHudLineVisible) (Object) message).drughack$getClientIdentifier().isEmpty()) continue;
                if (((IChatHudLineVisible) (Object) message).drughack$getClientIdentifier().equals(identifier)) removedVisibleLines.add(message);
            }

            ((IIChatHud) mc.inGameHud.getChatHud()).getMessages().removeAll(removedLines);
            ((IIChatHud) mc.inGameHud.getChatHud()).getVisibleMessages().removeAll(removedVisibleLines);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
 
    private String getPrefix() {
        return getPrefix(null);
    }

    private String getPrefix(String prefix) {
        return CustomFormatting.CLIENT + "[" + (prefix == null ? DrugHack.getInstance().getProtection().getName().replace(".cc", "") : prefix) + "]";
    }
}