package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.commands.Command;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.other.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("<load/save> <name> | <reload | save | current>", "Config", "saved or loaded a preset", "preset", "cfg", "config", "presets");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("load")
                        .then(arg("name", word())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    if (!FileUtils.fileExists(DrugHack.getInstance().getConfigDir() + File.separator + name + ".json")) {
                                        sendMessage("Config " + CustomFormatting.CLIENT + name + Formatting.WHITE + " don't exist");
                                        return SINGLE_SUCCESS;
                                    }
                                    try {
                                        DrugHack.getInstance().getConfigManager().loadModules(name);
                                        sendMessage("Config " + CustomFormatting.CLIENT + name + Formatting.WHITE + " successfully loaded");
                                        return SINGLE_SUCCESS;
                                    } catch (IOException exception) {
                                        sendMessage("Config " + CustomFormatting.CLIENT + name + Formatting.WHITE + " failed to load");
                                        return SINGLE_SUCCESS;
                                    }
                                })
                        )
                )
                .then(literal("save")
                        .then(arg("name", word())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    try {
                                        DrugHack.getInstance().getConfigManager().saveModules(name);
                                        sendMessage("Config " + CustomFormatting.CLIENT + name + Formatting.WHITE + " successfully saved");
                                        return SINGLE_SUCCESS;
                                    } catch (IOException exception) {
                                        sendMessage("Config " + CustomFormatting.CLIENT + name + Formatting.WHITE + " failed to save");
                                        return SINGLE_SUCCESS;
                                    }
                                })
                        )
                )
                .then(literal("save")
                        .executes(context -> {
                            DrugHack.getInstance().getConfigManager().saveAll();
                            sendMessage("Successfully saved current config");
                            return SINGLE_SUCCESS;
                        })
                )
                .then(literal("import")
                        .executes(context -> {
                            DrugHack.getInstance().getConfigManager().importConfig();
                            return SINGLE_SUCCESS;
                        })
                )
                .then(literal("export")
                        .executes(context -> {
                            DrugHack.getInstance().getConfigManager().exportConfig();
                            return SINGLE_SUCCESS;
                        })
                );
    }
}