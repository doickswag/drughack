package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import ru.drughack.commands.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GcCommand extends Command {

    public GcCommand() {
        super("empty", "Gc", "clean the memory", "gc");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            System.gc();
            return SINGLE_SUCCESS;
        });
    }
}