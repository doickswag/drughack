package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import ru.drughack.DrugHack;
import ru.drughack.commands.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class CloudCommand extends Command {

    public CloudCommand() {
        super("<save/load/update> <name>", "Cloud", "manage cloud configurations", "cloud");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("save")
                        .then(arg("name", word())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    DrugHack.getInstance().getConfigManager().saveCloudConfig(name, "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");
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("load")
                        .then(arg("name", word())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    DrugHack.getInstance().getConfigManager().loadCloudConfig(name, "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");
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("update")
                        .then(arg("name", word())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    DrugHack.getInstance().getConfigManager().updateCloudConfig(name, "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");
                                    return SINGLE_SUCCESS;
                                })
                        )
                );
    }
}