package ru.drughack.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import lombok.Getter;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import ru.drughack.DrugHack;

import java.util.List;

@Getter
public abstract class Command {
    private final String syntax, tag, description;
    private final List<String> names;

    public static void sendMessage(String message) {
        DrugHack.getInstance().getChatManager().message(message);
    }

    public Command(String syntax, String tag, String description, String... names) {
        this.names = List.of(names);
        this.syntax = syntax;
        this.tag = tag;
        this.description = description;
    }

    public abstract void execute(LiteralArgumentBuilder<CommandSource> builder);

    protected static <T> @NotNull RequiredArgumentBuilder<CommandSource, T> arg(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static @NotNull LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public void register(CommandDispatcher<CommandSource> dispatcher) {
        for (String name : names) {
            LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
            execute(builder);
            dispatcher.register(builder);
        }
    }
}