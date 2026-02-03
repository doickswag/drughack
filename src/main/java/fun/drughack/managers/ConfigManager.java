package fun.drughack.managers;

import com.google.gson.*;
import fun.drughack.DrugHack;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.client.Panic;
import fun.drughack.modules.impl.client.UI;
import fun.drughack.modules.settings.Setting;
import fun.drughack.modules.settings.api.*;
import fun.drughack.modules.settings.impl.*;
import fun.drughack.utils.macro.Macro;
import fun.drughack.utils.other.FileUtils;
import lombok.Cleanup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {

    private String currentConfig = "default";
    private final String extension = ".drug";

    public ConfigManager() {
        loadAll();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
    }

    private void loadAll() {
        try {
            loadGlobals();
            loadConfig(currentConfig);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void saveAll() {
        try {
            saveGlobals();
            saveConfig(currentConfig);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void saveConfig(String config) throws Exception {
        FileUtils.reset(DrugHack.getInstance().getConfigsDir() + "/" + config + extension);
        JsonObject object = new JsonObject();
        object.add("config", new JsonPrimitive(config));
        object.add("modules", serializeModules());
        @Cleanup OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(DrugHack.getInstance().getConfigsDir() + "/" + config + extension), StandardCharsets.UTF_8);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(object.toString())));
        this.currentConfig = config;
    }

    public void loadConfig(String config) throws Exception {
        if (!FileUtils.exists(DrugHack.getInstance().getConfigsDir() + "/" + config + extension)) return;
        InputStream stream = Files.newInputStream(Paths.get(DrugHack.getInstance().getConfigsDir() + "/" + config + extension));
        JsonObject object = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        if (object.has("modules")) deserializeModules(object.get("modules").getAsJsonObject());
        this.currentConfig = config;
    }

    public void saveGlobals() throws Exception {
        FileUtils.reset(DrugHack.getInstance().getGlobalsDir() + "/" + "globals" + extension);
        JsonObject object = new JsonObject();
        object.add("config", new JsonPrimitive(currentConfig));
        JsonArray friendsArray = new JsonArray();
        DrugHack.getInstance().getFriendManager().getFriends().forEach(friendsArray::add);
        object.add("friends", friendsArray);
        JsonArray macrosArray = new JsonArray();
        DrugHack.getInstance().getMacroManager().getMacros().forEach(macro -> macrosArray.add(macro.getName() + ":" + macro.getCommand() + ":" + macro.getBind().getKey()));
        object.add("macros", macrosArray);
        @Cleanup OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(DrugHack.getInstance().getGlobalsDir() + "/" + "globals" + extension), StandardCharsets.UTF_8);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(object.toString())));
    }

    public void loadGlobals() throws Exception {
        if (!FileUtils.exists(DrugHack.getInstance().getGlobalsDir() + "/" + "globals" + extension)) return;
        InputStream stream = Files.newInputStream(Paths.get(DrugHack.getInstance().getGlobalsDir() + "/" + "globals" + extension));
        JsonObject object = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

        if (object.has("config")) currentConfig = object.get("config").getAsString();
        if (object.has("friends")) {
            for (JsonElement element : object.get("friends").getAsJsonArray()) {
                if (DrugHack.getInstance().getFriendManager().isFriend(element.getAsString())) continue;
                DrugHack.getInstance().getFriendManager().add(element.getAsString());
            }
        }
        if (object.has("macros")) {
            for (JsonElement element : object.get("macros").getAsJsonArray()) {
                String[] split = element.getAsString().split(":", 3);
                if (split.length < 3) continue;
                String name =  split[0];
                String command = split[1];
                Bind bind = new Bind(Integer.parseInt(split[2]), false);
                DrugHack.getInstance().getMacroManager().add(new Macro(name, command, bind));
            }
        }
    }

    private JsonObject serializeModules() {
        JsonObject modules = new JsonObject();
        for (Module module : DrugHack.getInstance().getModuleManager().getModules()) {
            JsonObject object = new JsonObject();
            object.add("toggled", new JsonPrimitive(module.isToggled()));
            object.add("bind", new JsonPrimitive(module.getBind().getKey() + ", " + module.getBind().isMouse()));

            JsonObject settings = new JsonObject();
            for (Setting<?> s : module.getSettings()) {
                if (s instanceof BooleanSetting) settings.add(s.getName(), new JsonPrimitive((Boolean) s.getValue()));
                else if (s instanceof NumberSetting) settings.add(s.getName(), new JsonPrimitive((Float) s.getValue()));
                else if (s instanceof StringSetting) settings.add(s.getName(), new JsonPrimitive((String) s.getValue()));
                else if (s instanceof EnumSetting<?> enums) settings.add(s.getName(), new JsonPrimitive(((Nameable) enums.getValue()).getName()));
                else if (s instanceof BindSetting bind) settings.add(s.getName(), new JsonPrimitive(bind.getValue().getKey() + ", " + bind.getValue().isMouse()));
                else if (s instanceof ListSetting list) {
                    JsonObject list2 = new JsonObject();
                    for (BooleanSetting setting : list.getValue()) list2.add(setting.getName(), new JsonPrimitive(setting.getValue()));
                    settings.add(list.getName(), list2);
                }
            }

            object.add("settings", settings);
            modules.add(module.getName(), object);
        }

        for (HudElement element : DrugHack.getInstance().getHudManager().getHudElements()) {
            JsonObject object = new JsonObject();
            JsonObject settings = new JsonObject();
            for (Setting<?> s : element.getSettings()) {
            	if (s instanceof BooleanSetting) settings.add(s.getName(), new JsonPrimitive((Boolean) s.getValue()));
            	else if (s instanceof PositionSetting pos) settings.add(s.getName(), new JsonPrimitive(pos.getValue().getX() + ", " + pos.getValue().getY()));
            }
            object.add("settings", settings);
            modules.add(element.getName(), object);
        }

        return modules;
    }

    private void deserializeModules(JsonObject modules) {
        for (Module module : DrugHack.getInstance().getModuleManager().getModules()) {
            if (!modules.has(module.getName())) {
                module.setToggled(false);
                for (Setting<?> setting : module.getSettings()) setting.reset();
                continue;
            }

            JsonObject object = modules.get(module.getName()).getAsJsonObject();
            if (!(module instanceof UI || module instanceof Panic)) module.setToggled(object.has("toggled") && object.get("toggled").getAsBoolean());

            if (object.has("bind")) {
                String[] data = object.get("bind").getAsString().split(", ");
                if (data.length == 2) {
                    int key = Integer.parseInt(data[0]);
                    boolean mouse = Boolean.parseBoolean(data[1]);
                    module.setBind(new Bind(key, mouse));
                }
            }

            if (!object.has("settings")) {
                for (Setting<?> setting : module.getSettings()) setting.reset();
                continue;
            }

            JsonObject settings = object.get("settings").getAsJsonObject();

            for (Setting<?> s : module.getSettings()) {
                if (!settings.has(s.getName())) {
                    s.reset();
                    continue;
                }

                JsonElement element = settings.get(s.getName());
                if (s instanceof BooleanSetting) ((BooleanSetting) s).setValue(element.getAsBoolean());
                else if (s instanceof NumberSetting) ((NumberSetting) s).setValue(element.getAsFloat());
                else if (s instanceof EnumSetting<?>) ((EnumSetting<?>) s).setEnumValue(element.getAsString());
                else if (s instanceof StringSetting) ((StringSetting) s).setValue(element.getAsString());
                else if (s instanceof BindSetting) {
                    String[] data = element.getAsString().split(", ");
                    if (data.length == 2) {
                        int key = Integer.parseInt(data[0]);
                        boolean mouse = Boolean.parseBoolean(data[1]);
                        ((BindSetting) s).setValue(new Bind(key, mouse));
                    }
                } else if (s instanceof ListSetting list) {
                    JsonObject list2 = element.getAsJsonObject();
                    for (BooleanSetting setting : list.getValue()) if (list2.has(setting.getName())) setting.setValue(list2.get(setting.getName()).getAsBoolean());
                }
            }
        }

        for (HudElement element : DrugHack.getInstance().getHudManager().getHudElements()) {
            if (!modules.has(element.getName())) {
                element.setToggled(false);
                for (Setting<?> setting : element.getSettings()) setting.reset();
                continue;
            }

            JsonObject object = modules.get(element.getName()).getAsJsonObject();
            
            if (!object.has("settings")) {
                for (Setting<?> setting : element.getSettings()) setting.reset();
                continue;
            }

            JsonObject settings = object.get("settings").getAsJsonObject();
            for (Setting<?> s : element.getSettings()) {
                if (!settings.has(s.getName())) {
                    s.reset();
                    continue;
                }

                JsonElement element2 = settings.get(s.getName());
                if (s instanceof BooleanSetting) ((BooleanSetting) s).setValue(element2.getAsBoolean());
                else if (s instanceof PositionSetting) {
                    String[] data = element2.getAsString().split(", ");
                    if (data.length == 2) {
                        float x = Float.parseFloat(data[0]);
                        float y = Float.parseFloat(data[1]);
                        ((PositionSetting) s).setValue(new Position(x, y));
                    }
                }
            }
        }
    }
}