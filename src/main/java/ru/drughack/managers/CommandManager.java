package ru.drughack.managers;

import com.mojang.brigadier.CommandDispatcher;
import lombok.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.commands.Command;
import ru.drughack.commands.impl.*;
import ru.drughack.modules.api.Module;

import java.util.*;

@Getter
@Setter
public class CommandManager {
    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
    private final List<Command> commands = new ArrayList<>();
    private String prefix = ".";

    public CommandManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
        addCommands(
                new CloudCommand(),
                new BindCommand(),
                new FriendCommand(),
                new ConfigCommand(),
                new FolderCommand(),
                new HelpCommand(),
                new DomainCommand(),
                new FakePlayerCommand(),
                new GcCommand()
        );
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (Module.fullNullCheck()) return;
        setPrefix(DrugHack.getInstance().getModuleManager().getUi().prefix.getValue());
    }

    private void addCommands(@NotNull Command... command) {
        for (Command cmd : command) {
            cmd.register(dispatcher);
            commands.add(cmd);
        }
    }
}