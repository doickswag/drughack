package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.commands.Command;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.formatting.FormattingUtils;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("[command]", "Help", "displays command list or information about a command", "help");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .executes(context -> {
                    List<Command> commands = DrugHack.getInstance().getCommandManager().getCommands();
                    if (commands.isEmpty()) {
                        sendMessage("There are currently no registered commands");
                    } else {
                        StringBuilder builder1 = new StringBuilder();
                        int commandCount = 0;
                        int totalNames = 0;

                        for (Command command : commands) {
                            totalNames += command.getNames().size();
                        }

                        for (Command command : commands) {
                            for (String name : command.getNames()) {
                                commandCount++;
                                builder1.append(Formatting.WHITE).append(name)
                                        .append(commandCount == totalNames ? "" : ", ");
                            }
                        }
                        sendMessage("Commands [" + totalNames + "]: " + builder1);
                    }
                    return SINGLE_SUCCESS;
                })
                .then(arg("command", word())
                        .suggests((context, builder1) -> {
                            DrugHack.getInstance().getCommandManager().getCommands().stream()
                                    .flatMap(command -> command.getNames().stream())
                                    .filter(name -> name.startsWith(builder1.getRemaining()))
                                    .forEach(builder1::suggest);
                            return builder1.buildFuture();
                        })
                        .executes(context -> {
                            String commandName = StringArgumentType.getString(context, "command");
                            Command command = DrugHack.getInstance().getCommandManager().getCommands().stream()
                                    .filter(cmd -> cmd.getNames().contains(commandName))
                                    .findFirst()
                                    .orElse(null);

                            if (command == null) {
                                sendMessage("Could not find the command");
                                return SINGLE_SUCCESS;
                            }

                            String names = String.join(", ", command.getNames());
                            sendMessage(command.getTag() + CustomFormatting.CLIENT + " - " + Formatting.WHITE + names + " " + command.getSyntax());
                            sendMessage(command.getDescription());
                            return SINGLE_SUCCESS;
                        })
                );
    }
}