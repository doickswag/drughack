package ru.drughack.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;
import ru.drughack.commands.Command;
import ru.drughack.modules.api.Module;
import ru.drughack.DrugHack;

import java.lang.reflect.Field;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class BindCommand extends Command {

    public BindCommand() {
        super("<add/set> <toggle/hold> <module> <key> | <remove/delete> <module>", "Bind", "bind the modules", "bind");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("set")
                        .then(arg("type", word())
                                .suggests((context, suggestionsBuilder) -> suggestionsBuilder.suggest("toggle").suggest("hold").buildFuture())
                                .then(arg("module", word())
                                        .suggests((context, builder1) -> {
                                            DrugHack.getInstance().getModuleManager().getModules().stream()
                                                    .map(Module::getName)
                                                    .filter(name -> name.startsWith(builder1.getRemaining()))
                                                    .forEach(builder1::suggest);
                                            return builder1.buildFuture();
                                        })
                                        .then(arg("key", word())
                                                .suggests((context, builder1) -> {
                                                    for (Field field : GLFW.class.getDeclaredFields()) {
                                                        String name = field.getName();
                                                        if (name.startsWith("GLFW_KEY_") || name.startsWith("GLFW_MOUSE_BUTTON_")) {
                                                            String key = name
                                                                    .replace("GLFW_KEY_", "")
                                                                    .replace("GLFW_MOUSE_BUTTON_", "MOUSE_");
                                                            if (key.startsWith(builder1.getRemaining())) builder1.suggest(key);
                                                        }
                                                    }
                                                    if ("NONE".startsWith(builder1.getRemaining())) builder1.suggest("NONE");
                                                    return builder1.buildFuture();
                                                })
                                                .executes(context -> {
                                                    String type = StringArgumentType.getString(context, "type");
                                                    String module = StringArgumentType.getString(context, "module");
                                                    String key = StringArgumentType.getString(context, "key").replace("", "");
                                                    Bind(type, module, key);
                                                    return SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .then(arg("module", word())
                                .suggests((context, builder1) -> {
                                    DrugHack.getInstance().getModuleManager().getModules().stream()
                                            .map(Module::getName)
                                            .filter(name -> name.startsWith(builder1.getRemaining()))
                                            .forEach(builder1::suggest);
                                    return builder1.buildFuture();
                                })
                                .executes(context -> {
                                    String module = StringArgumentType.getString(context, "module");
                                    UnBind(module);
                                    return SINGLE_SUCCESS;
                                })
                        )
                );
    }

    private void Bind(String type, String moduleStr, String key) {
        Module module = DrugHack.getInstance().getModuleManager().getModule(moduleStr);
        if (module == null) {
            sendMessage("Module '" + moduleStr + "' not found!");
            return;
        }

        boolean holding = type.equalsIgnoreCase("hold");
        boolean mouse = key.toLowerCase().startsWith("mouse");
        int keyCode = 0;

        try {
            if (key.equalsIgnoreCase("NONE")) {
                keyCode = -1;
            } else {
                if (mouse) {
                    String glfwMouse = "GLFW_MOUSE_BUTTON_" + key.substring(6).toUpperCase();
                    try {
                        keyCode = GLFW.class.getField(glfwMouse).getInt(null);
                    } catch (NoSuchFieldException e) {
                        sendMessage("This mouse key does not exist!");
                    }
                } else {
                    String glfwKey = "GLFW_KEY_" + key.toUpperCase();
                    try {
                        keyCode = GLFW.class.getField(glfwKey).getInt(null);
                    } catch (NoSuchFieldException e) {
                        sendMessage("This key does not exist!");
                    }
                }
            }

            if (keyCode == 0) {
                sendMessage("Unknown key '" + key + "'!");
                return;
            }

            module.setBind(-1, false, false);
            if (holding) module.setToggled(false);
            module.setBind(keyCode, holding, mouse);
            sendMessage("Installed bind " + key.toUpperCase() + " for module " + module.getName());
        } catch (Exception e) {
            sendMessage("Invalid key! Use English letters");
        }
    }

    private void UnBind(String moduleStr) {
        Module module = DrugHack.getInstance().getModuleManager().getModule(moduleStr);

        if (module == null) {
            sendMessage("Module '" + moduleStr + "' not found!");
            return;
        }

        if (module.getBind().isEmpty()) {
            sendMessage("Module '" + module.getName() + "' has no bind!");
            return;
        }

        module.setBind(-1, false, false);
        sendMessage("Uninstalled bind for module " + module.getName());
    }
}