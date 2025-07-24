package ru.drughack.api.mixins.accesors;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public interface IChatInputSuggestor {

    @Accessor("pendingSuggestions")
    CompletableFuture<Suggestions> getSuggestion();

    @Accessor("window")
    ChatInputSuggestor.SuggestionWindow getWindow();

    @Accessor("parse")
    ParseResults<CommandSource> getParse();
}