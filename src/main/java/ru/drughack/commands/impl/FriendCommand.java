package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.commands.Command;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("<add/remove> <player> | list", "Friend", "added/removed friend from list", "friend", "fr");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("add")
                        .then(arg("player", word())
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    if (!DrugHack.getInstance().getFriendManager().contains(player)) {
                                        DrugHack.getInstance().getFriendManager().add(player);
                                        sendMessage("Player " + player + " has been added to your friend list");
                                    } else {
                                        sendMessage("Player " + player + " is already on your friend list");
                                    }
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("remove")
                        .then(arg("player", word())
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    if (DrugHack.getInstance().getFriendManager().contains(player)) {
                                        DrugHack.getInstance().getFriendManager().remove(player);
                                        sendMessage("Player " + player + " has been removed from your friend list");
                                    } else {
                                        sendMessage("Player " + player + " is not on your friend list");
                                    }
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("del")
                        .then(arg("player", word())
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    if (DrugHack.getInstance().getFriendManager().contains(player)) {
                                        DrugHack.getInstance().getFriendManager().remove(player);
                                        sendMessage("Player " + player + " has been removed from your friend list");
                                    } else {
                                        sendMessage("Player " + player + " is not on your friend list");
                                    }
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("delete")
                        .then(arg("player", word())
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    if (DrugHack.getInstance().getFriendManager().contains(player)) {
                                        DrugHack.getInstance().getFriendManager().remove(player);
                                        sendMessage("Player " + player + " has been removed from your friend list");
                                    } else {
                                        sendMessage("Player " + player + " is not on your friend list");
                                    }
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("list")
                        .executes(context -> {
                            List<String> friends = DrugHack.getInstance().getFriendManager().getFriends();
                            if (friends.isEmpty()) {
                                sendMessage("You currently have no friends");
                            } else {
                                StringBuilder builder1 = new StringBuilder();
                                int index = 0;
                                for (String name : friends) {
                                    index++;
                                    builder1.append(Formatting.WHITE).append(name).append(index == friends.size() ? "" : ", ");
                                }
                                sendMessage("Friends [" + friends.size() + "]: " + builder1);
                            }
                            return SINGLE_SUCCESS;
                        })
                );
    }
}