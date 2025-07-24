package ru.drughack.api.mixins.accesors;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public interface ISuggestionWindow {

    @Accessor("selection")
    int getSelection();

    @Accessor("typedText")
    String getTypedText();
}