package fun.drughack.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fun.drughack.utils.Wrapper;
import lombok.Getter;
import net.minecraft.command.CommandSource;

@Getter
public abstract class Command implements Wrapper {
    private final String command;

    public Command(String command) {
        this.command = command;
    }

    public abstract void execute(LiteralArgumentBuilder<CommandSource> builder);

    public void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(command);
        execute(builder);
        dispatcher.register(builder);
    }

    protected <T> RequiredArgumentBuilder<CommandSource, T> arg(String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }
}