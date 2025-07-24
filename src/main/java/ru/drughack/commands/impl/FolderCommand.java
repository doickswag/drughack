package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import ru.drughack.DrugHack;
import ru.drughack.commands.Command;

import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FolderCommand extends Command {

    public FolderCommand() {
        super("", "Folder", "opens the client folder", "folder", "f");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .executes(context -> {
                    try {
                        String os = System.getProperty("os.name").toLowerCase();

                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("explorer.exe " + DrugHack.getInstance().getClientDir().getAbsolutePath());
                        } else if (os.contains("mac")) {
                            Runtime.getRuntime().exec("open " + DrugHack.getInstance().getClientDir().getAbsolutePath());
                        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                            Runtime.getRuntime().exec("xdg-open " + DrugHack.getInstance().getClientDir().getAbsolutePath());
                        } else {
                            sendMessage("Unsupported operating system for opening the folder.");
                            return SINGLE_SUCCESS;
                        }
                        return SINGLE_SUCCESS;
                    } catch (IOException ignored) {
                        sendMessage("Failed to open the client folder.");
                        return SINGLE_SUCCESS;
                    }
                });
    }
}