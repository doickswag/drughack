package ru.drughack.api.mixins;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.drughack.DrugHack;
import ru.drughack.api.mixins.accesors.IChatInputSuggestor;
import ru.drughack.api.mixins.accesors.ISuggestionWindow;
import ru.drughack.api.event.impl.EventChatInput;
import ru.drughack.utils.render.ColorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Unique Color color = ColorUtils.getGlobalColor();

    @Shadow protected TextFieldWidget chatField;
    @Shadow ChatInputSuggestor chatInputSuggestor;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"), cancellable = true)
    private void sendMessage(String chatText, boolean addToHistory, CallbackInfo info) {
        EventChatInput event = new EventChatInput(chatText);
        DrugHack.getInstance().getEventHandler().post(event);
        if (event.isCanceled()) info.cancel();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (chatField.getText().startsWith(DrugHack.getInstance().getCommandManager().getPrefix())
                || (DrugHack.getInstance().getModuleManager().getIrc().isToggled()
                && DrugHack.getInstance().getModuleManager().getIrc().chat.getValue().isEnabled()
                && chatField.getText().startsWith(DrugHack.getInstance().getModuleManager().getIrc().prefix.getValue())))
            context.drawBorder(1, this.height - 15, (this.width - 2), 14, color.getRGB());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = Shift.BEFORE))
    private void renderHook2(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (chatField.getText().startsWith(DrugHack.getInstance().getCommandManager().getPrefix())) {
            ISuggestionWindow window = (ISuggestionWindow) ((IChatInputSuggestor) this.chatInputSuggestor).getWindow();
            CompletableFuture<Suggestions> future = ((IChatInputSuggestor) this.chatInputSuggestor).getSuggestion();

            if (future != null && future.isDone() && this.chatField.getText().startsWith(DrugHack.getInstance().getCommandManager().getPrefix())) {
                String str = "";
                Suggestions suggest = future.join();
                ParseResults<CommandSource> parse = ((IChatInputSuggestor) this.chatInputSuggestor).getParse();
                List<String> list = Collections.emptyList();

                if (parse != null && this.chatField.getCursor() == this.chatField.getText().length()) {
                    List<ParsedCommandNode<CommandSource>> list2 = parse.getContext().getNodes();
                    if (!list2.isEmpty()) list = this.getStrings(list2.getLast().getNode().getChildren());
                }

                if (window != null && !suggest.getList().isEmpty() && window.getSelection() < suggest.getList().size()) {
                    String str2 = suggest.getList().get(window.getSelection()).apply(window.getTypedText());
                    str = str + (str2.startsWith(this.chatField.getText()) ? str2.substring(this.chatField.getText().length()) : "");
                }

                if ((this.chatField.getCursor() > 0 && this.chatField.getText().charAt(this.chatField.getCursor() - 1) != ' ') || !str.isEmpty()) str = str + " ";
                if (parse != null && parse.getReader().canRead() && (parse.getReader().peek() != ' ' || window != null) && !list.isEmpty()) list.removeFirst();
                str = str + String.join(" ", list);
                this.chatField.setSuggestion(str);
            }
        }
    }

    @Unique
    private List<String> getStrings(Collection<CommandNode<CommandSource>> nodes) {
        ArrayList<String> array = new ArrayList<>();

        while (true) {
            CommandNode<CommandSource> node = null;
            int i = 0;
            StringBuilder builder = new StringBuilder("<");

            for (CommandNode<CommandSource> commandNode : nodes) {
                ++i;
                builder.append(commandNode.getName());
                if (i != nodes.size()) builder.append(", ");
                else node = commandNode;
            }

            builder.append(">");
            if (!nodes.isEmpty()) array.add(builder.toString());
            if (nodes.size() != 1 || node == null) return array;
            nodes = node.getChildren();
        }
    }
}