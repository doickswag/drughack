package ru.drughack.managers;

import com.google.gson.*;
import lombok.*;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.gui.api.Button;
import ru.drughack.gui.api.Frame;
import ru.drughack.modules.api.HudModule;
import ru.drughack.gui.impl.ColorButton;
import ru.drughack.gui.impl.ModuleButton;
import ru.drughack.modules.impl.client.HudEditor;
import ru.drughack.modules.impl.client.UI;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.modules.settings.impl.Bind;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.modules.settings.impl.ColorSetting;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.impl.PosSetting;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.other.FileUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class ConfigManager implements Wrapper {

    private String currentConfig = "default";
    private final String GITHUB_API_URL = "https://api.github.com/repos/serverattacked/cloudconfigs/contents/";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ConfigManager() {
        loadAll();
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
    }

    public void saveAll() {
        try {
            saveModules(currentConfig);
            saveGeneral();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void loadAll() {
        try {
            loadGeneral();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void saveModules(String config) throws IOException {
        FileUtils.resetFile(DrugHack.getInstance().getConfigDir() + File.separator + config + ".json");
        JsonObject configObject = new JsonObject();
        configObject.add("config", new JsonPrimitive(config));
        configObject.add("modules", serializeCurrentModules());
        @Cleanup OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(DrugHack.getInstance().getConfigDir() + File.separator + config + ".json"), StandardCharsets.UTF_8);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(configObject.toString())));
        this.currentConfig = config;
    }

    public void loadModules(String config) throws IOException {
        if (!FileUtils.fileExists(DrugHack.getInstance().getConfigDir() + File.separator + config + ".json")) return;
        @Cleanup InputStream stream = Files.newInputStream(Paths.get(DrugHack.getInstance().getConfigDir() + File.separator + config + ".json"));
        JsonObject configObject = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        if (configObject.has("modules")) deserializeModules(configObject.get("modules").getAsJsonObject());
        this.currentConfig = config;
    }

    public void saveGeneral() throws IOException {
        FileUtils.resetFile(DrugHack.getInstance().getClientDir() + File.separator + "general.json");
        JsonObject configObject = new JsonObject();
        configObject.add("config", new JsonPrimitive(currentConfig));
        configObject.add("prefix", new JsonPrimitive(DrugHack.getInstance().getCommandManager().getPrefix()));
        JsonArray friendsArray = new JsonArray();
        DrugHack.getInstance().getFriendManager().getFriends().forEach(friendsArray::add);
        configObject.add("friends", friendsArray);
        @Cleanup OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(DrugHack.getInstance().getClientDir() + File.separator + "general.json"), StandardCharsets.UTF_8);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(configObject.toString())));
    }

    public void loadGeneral() throws IOException {
        if (!FileUtils.fileExists(DrugHack.getInstance().getClientDir() + File.separator + "general.json")) return;
        @Cleanup InputStream stream = Files.newInputStream(Paths.get(DrugHack.getInstance().getClientDir() + File.separator + "general.json"));
        JsonObject configObject;

        try {
            configObject = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        } catch (IllegalStateException exception) {
            return;
        }

        if (configObject.has("config")) currentConfig = configObject.get("config").getAsString();
        if (configObject.has("prefix")) DrugHack.getInstance().getCommandManager().setPrefix(configObject.get("prefix").getAsString());
        if (configObject.has("friends")) {
            for (JsonElement element : configObject.get("friends").getAsJsonArray()) {
                if (DrugHack.getInstance().getFriendManager().contains(element.getAsString())) continue;
                DrugHack.getInstance().getFriendManager().add(element.getAsString());
            }
        }
    }

    public void saveCloudConfig(String configName, String githubToken) {
        try {
            HttpURLConnection checkConnection = createGitHubConnection(configName + ".json", githubToken, "GET");
            int checkResponseCode = checkConnection.getResponseCode();
            if (checkResponseCode == 200) {
                DrugHack.getInstance().getChatManager().await("Error: Config " + CustomFormatting.CLIENT + configName + Formatting.WHITE + " already exist. Use command 'update' for update the config.");
                return;
            } else if (checkResponseCode != 404) {
                String error = new String(checkConnection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                DrugHack.getInstance().getChatManager().await("Error check file: HTTP " + checkResponseCode + " - " + error);
                return;
            }

            JsonObject config = new JsonObject();
            config.add("modules", serializeCurrentModules());
            String content = new GsonBuilder().setPrettyPrinting().create().toJson(config);
            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
            HttpURLConnection connection = createGitHubConnection(configName + ".json", githubToken, "PUT");
            String payload = String.format(
                    "{\"message\":\"new config!\", \"content\":\"%s\"}",
                    base64Content
            );

            connection.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                DrugHack.getInstance().getChatManager().await("Config " + CustomFormatting.CLIENT + configName + Formatting.WHITE + " successfully create in cloud");
            } else {
                String error = new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                DrugHack.getInstance().getChatManager().await("Error creation: HTTP " + responseCode + " - " + error);
            }
        } catch (Exception e) {
            DrugHack.getInstance().getChatManager().await("Error creation config: " + e.getMessage());
        }
    }

    public void updateCloudConfig(String configName, String githubToken) {
        try {
            HttpURLConnection getConnection = createGitHubConnection(configName + ".json", githubToken, "GET");

            if (getConnection.getResponseCode() == 404) {
                DrugHack.getInstance().getChatManager().await("Config " + CustomFormatting.CLIENT + configName + " not found in cloud. Use 'save' command first.");
                return;
            }

            JsonObject fileInfo = JsonParser.parseReader(new InputStreamReader(getConnection.getInputStream())).getAsJsonObject();
            String sha = fileInfo.get("sha").getAsString();
            JsonObject config = new JsonObject();
            config.add("modules", serializeCurrentModules());
            String content = new GsonBuilder().setPrettyPrinting().create().toJson(config);
            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
            HttpURLConnection putConnection = createGitHubConnection(configName + ".json", githubToken, "PUT");
            putConnection.setDoOutput(true);

            String payload = String.format(
                    "{\"message\":\"update config\",\"content\":\"%s\",\"sha\":\"%s\"}",
                    base64Content,
                    sha
            );

            try (OutputStream os = putConnection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = putConnection.getResponseCode();
            if (responseCode == 200) {
                DrugHack.getInstance().getChatManager().await("Config " + CustomFormatting.CLIENT + configName + " successfully updated in cloud");
            } else {
                String error = new String(putConnection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                DrugHack.getInstance().getChatManager().await("Update failed (" + responseCode + "): " + error);
            }
        } catch (Exception e) {
            DrugHack.getInstance().getChatManager().await("Update error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public void loadCloudConfig(String configName, String githubToken) {
        try {
            HttpURLConnection connection = createGitHubConnection(configName + ".json", githubToken, "GET");
            JsonObject fileInfo = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            String base64Content = fileInfo.get("content").getAsString().replace("\n", "");
            String content = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);
            JsonObject configObject = JsonParser.parseString(content).getAsJsonObject();
            if (configObject.has("modules")) deserializeModules(configObject.get("modules").getAsJsonObject());
            DrugHack.getInstance().getChatManager().await("Config " + CustomFormatting.CLIENT + configName + " successfully loaded from cloud");
            this.currentConfig = configName;
        } catch (Exception e) {
            DrugHack.getInstance().getChatManager().await("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public void exportConfig() {
        try {
            JsonObject configObject = new JsonObject();
            configObject.add("config", new JsonPrimitive(currentConfig));
            configObject.add("modules", serializeCurrentModules());
            String configJson = new GsonBuilder().setPrettyPrinting().create().toJson(configObject);

            if (configJson == null || configJson.isEmpty()) {
                DrugHack.getInstance().getChatManager().await("Error: Config could not be generated");
                return;
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(configJson),
                    null
            );

            DrugHack.getInstance().getChatManager().await("Config successfully exported to clipboard");
        } catch (IllegalStateException e) {
            DrugHack.getInstance().getChatManager().await("Error: clipboard not available (try again)");
        } catch (Exception e) {
            DrugHack.getInstance().getChatManager().await("Export error: " + (e.getMessage() != null ? e.getMessage() : "unknown error"));
            DrugHack.LOGGER.error("Export to clipboard error", e);
        }
    }

    public void importConfig() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                DrugHack.getInstance().getChatManager().await("The clipboard does not contain text data");
                return;
            }

            String configJson = (String) clipboard.getData(DataFlavor.stringFlavor);

            if (configJson == null || configJson.trim().isEmpty()) {
                DrugHack.getInstance().getChatManager().await("The clipboard contains empty data");
                return;
            }

            JsonObject configObject = JsonParser.parseString(configJson).getAsJsonObject();

            if (configObject.has("modules")) {
                deserializeModules(configObject.get("modules").getAsJsonObject());
                DrugHack.getInstance().getChatManager().await("Config successfully imported");
                if (configObject.has("config")) this.currentConfig = configObject.get("config").getAsString();
            } else {
                DrugHack.getInstance().getChatManager().await("Error: no module information in the data");
            }
        } catch (UnsupportedFlavorException e) {
            DrugHack.getInstance().getChatManager().await("Error: unsupported buffer data format");
        } catch (IOException e) {
            DrugHack.getInstance().getChatManager().await("Error accessing clipboard");
        } catch (JsonParseException e) {
            DrugHack.getInstance().getChatManager().await("Error: invalid JSON format in buffer");
        } catch (IllegalStateException e) {
            DrugHack.getInstance().getChatManager().await("Clipboard is not available (try again)");
        } catch (Exception e) {
            DrugHack.getInstance().getChatManager().await("Import error: " + (e.getMessage() != null ? e.getMessage() : "unknown error"));
            DrugHack.LOGGER.error("Import from clipboard error", e);
        }
    }

    //API FOR CONFIGS (SWAG)

    private HttpURLConnection createGitHubConnection(String path, String token, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_API_URL + path).openConnection();

        executor.submit(() -> {
            try {
                connection.setDoOutput(true);
                connection.setRequestMethod(method);
                connection.setRequestProperty("Authorization", "token " + token);
                connection.setRequestProperty("Content-Type", "application/json");
            } catch (IOException ignored) {}
        });

        return connection;
    }

    private JsonObject serializeCurrentModules() {
        JsonObject modules = new JsonObject();
        for (Module module : DrugHack.getInstance().getModuleManager().getModules()) {
            JsonObject moduleData = new JsonObject();
            moduleData.add("enabled", new JsonPrimitive(module.isToggled()));

            JsonObject settings = new JsonObject();
            for (Setting<?> s : module.getSettings()) {
                if (s.getValue() instanceof Boolean) {
                    settings.add(s.getName(), new JsonPrimitive((Boolean) s.getValue()));
                } else if (s.getValue() instanceof Float) {
                    settings.add(s.getName(), new JsonPrimitive((Float) s.getValue()));
                } else if (s.getValue() instanceof Integer) {
                    settings.add(s.getName(), new JsonPrimitive((Integer) s.getValue()));
                } else if (s.getValue() instanceof String) {
                    settings.add(s.getName(), new JsonPrimitive((String) s.getValue()));
                } else if (s.getValue() instanceof Enum<?>) {
                    settings.add(s.getName(), new JsonPrimitive(((Nameable) s.getValue()).getName()));
                } else if (s.getValue() instanceof Bind bind) {
                    if (!(module instanceof HudModule)) settings.add(s.getName(), new JsonPrimitive(bind.getKey() + ", " + bind.isHold() + ", " + bind.isMouse()));
                } else if (s.getValue() instanceof ColorSetting cs) {
                    Color color = cs.getValue().getColor();
                    settings.add(s.getName(), new JsonPrimitive(
                            String.format("rgb { %d, %d, %d, %d, %b }",
                                    color.getRed(),
                                    color.getGreen(),
                                    color.getBlue(),
                                    color.getAlpha(),
                                    cs.isSync())
                    ));
                } else if (s.getValue() instanceof CategoryBooleanSetting cbs) {
                    settings.add(s.getName(), new JsonPrimitive(cbs.isEnabled()));
                } else if (s.getValue() instanceof PosSetting ps) {
                    JsonObject posObject = new JsonObject();
                    posObject.add("x", new JsonPrimitive(ps.getX()));
                    posObject.add("y", new JsonPrimitive(ps.getY()));
                    settings.add(s.getName(), posObject);
                }
            }
            moduleData.add("settings", settings);
            modules.add(module.getName(), moduleData);
        }
        return modules;
    }

    private void deserializeModules(JsonObject modulesObject) {
        for (Module module : DrugHack.getInstance().getModuleManager().getModules()) {
            if (!modulesObject.has(module.getName())) {
                module.setToggled(false);
                module.resetValues();
                continue;
            }

            JsonObject moduleObject = modulesObject.get(module.getName()).getAsJsonObject();
            module.setToggled(moduleObject.has("enabled") && moduleObject.get("enabled").getAsBoolean());
            if (module instanceof HudEditor || module instanceof UI) module.setToggled(false);
            if (module.getBind().isHold()) module.setToggled(false);

            if (!moduleObject.has("settings")) {
                module.resetValues();
                continue;
            }

            JsonObject settings = moduleObject.get("settings").getAsJsonObject();

            for (Setting<?> s : module.getSettings()) {
                if (!settings.has(s.getName())) {
                    s.reset();
                    continue;
                }

                JsonElement valueObject = settings.get(s.getName());

                try {
                    if (s.getValue() instanceof Boolean) {
                        ((Setting<Boolean>) s).setValue(valueObject.getAsBoolean());
                    } else if (s.getValue() instanceof Float) {
                        ((Setting<Float>) s).setValue(valueObject.getAsFloat());
                    } else if (s.getValue() instanceof Integer) {
                        ((Setting<Integer>) s).setValue(valueObject.getAsInt());
                    } else if (s.getValue() instanceof Enum) {
                        s.setEnumValue(valueObject.getAsString());
                    } else if (s.getValue() instanceof String) {
                        ((Setting<String>) s).setValue(valueObject.getAsString());
                    } else if (s.getValue() instanceof Bind) {
                        String[] bindData = valueObject.getAsString().split(", ");
                        if (bindData.length == 3) {
                            int key = Integer.parseInt(bindData[0]);
                            boolean holding = Boolean.parseBoolean(bindData[1]);
                            boolean isMouse = Boolean.parseBoolean(bindData[2]);
                            ((Setting<Bind>) s).setValue(new Bind(key, holding, isMouse));
                        }
                    } else if (s.getValue() instanceof ColorSetting cs) {
                        String value = valueObject.getAsString();
                        if (value.startsWith("rgb {")) {
                            try {
                                String inner = value.substring(5, value.length() - 1).trim();
                                String[] parts = inner.split("\\s*,\\s*");

                                if (parts.length == 5) {
                                    int r = Integer.parseInt(parts[0].trim());
                                    int g = Integer.parseInt(parts[1].trim());
                                    int b = Integer.parseInt(parts[2].trim());
                                    int a = Integer.parseInt(parts[3].trim());
                                    boolean sync = Boolean.parseBoolean(parts[4].trim());

                                    cs.getValue().setColor(new Color(
                                            Math.min(Math.max(r, 0), 255),
                                            Math.min(Math.max(g, 0), 255),
                                            Math.min(Math.max(b, 0), 255),
                                            Math.min(Math.max(a, 0), 255)
                                    ));

                                    cs.setSync(sync);

                                    for (Frame frame : DrugHack.getInstance().getClickGuiScreen().getFrames())
                                        for (Button button : frame.getButtons())
                                            if (button instanceof ModuleButton mb && mb.open)
                                                for (Button but : mb.buttons)
                                                    if (but instanceof ColorButton cb && cb.setting == s)
                                                        cb.updateHSB();
                                }
                            } catch (Exception e) {
                                DrugHack.LOGGER.error("Error parsing color: {}", value, e);
                            }
                        }
                    } else if (s.getValue() instanceof CategoryBooleanSetting) {
                        ((Setting<CategoryBooleanSetting>) s).setValue(new CategoryBooleanSetting(valueObject.getAsBoolean()));
                    } else if (s.getValue() instanceof PosSetting) {
                        if (valueObject.isJsonObject()) {
                            JsonObject posObject = valueObject.getAsJsonObject();
                            float x = posObject.get("x").getAsFloat();
                            float y = posObject.get("y").getAsFloat();
                            ((Setting<PosSetting>) s).setValue(new PosSetting(x, y));

                            if (module instanceof HudModule hudModule) {
                                hudModule.setBounds(
                                        x * (mc.getWindow() != null ? mc.getWindow().getScaledWidth() : 1),
                                        y * (mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 1),
                                        hudModule.getWidth(),
                                        hudModule.getHeight()
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    DrugHack.LOGGER.error("Error loading setting {} for module {}: {}", s.getName(), module.getName(), e.getMessage());
                }
            }
        }
    }
}