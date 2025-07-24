package ru.drughack.api.protection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.modules.api.Module;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.math.TimerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Gvobavs implements Wrapper {

    private final Gson gson = new Gson();
    private Set<String> players = new LinkedHashSet<>();
    private final TimerUtils pingTimer = new TimerUtils();
    private List<String> THOnlinePlayers = new ArrayList<>();
    private List<String> THAllPlayers = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Gvobavs() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::onDisconnect));
    }

    public void updateTHData() {
        executor.execute(() -> {
            THOnlinePlayers = getTHPlayers(true);
            THAllPlayers = getTHPlayers(false);
        });
    }

    public List<String> getTHPlayers(boolean online) {
        final List<String> names = new ArrayList<>();

        try {
            String response = get("https://api.thunderhack.net/v1/users" + (online ? "/online" : ""));
            JsonArray array = JsonParser.parseString(response).getAsJsonArray();
            array.forEach(e -> names.add(e.getAsJsonObject().get("name").getAsString()));
        }  catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return names;
    }

    public String get(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + DrugHack.getInstance().getProtection().getToken());
        connection.setConnectTimeout(5000);


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public String post(String url, String content) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + DrugHack.getInstance().getProtection().getToken());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = content.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public void onConnect() {
        onAdd();
        onUpdate();
    }

    public void onDisconnect() {
       onRemove();
    }

    private void onAdd() {
        executor.execute(() -> {
            try {
                post("http://f1.rustix.me:41722/irc/add", mc.getSession().getUsername());
                if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("IRC", Formatting.GREEN + "Successfully connected to the server");
            } catch (Exception e) {
                if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("IRC", Formatting.RED + e.getMessage());
                e.printStackTrace(System.err);
            }
        });
    }

    private void onRemove() {
        if (isConnected()) {
            executor.execute(() -> {
                try {
                    players = Collections.emptySet();
                    THOnlinePlayers = Collections.emptyList();
                    THAllPlayers = Collections.emptyList();
                    post("http://f1.rustix.me:41722/irc/remove", mc.getSession().getUsername());
                    if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("IRC", Formatting.RED + "Successfully disconnected the from server");
                } catch (Exception e) {
                    if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("IRC", Formatting.RED + e.getMessage());
                    e.printStackTrace(System.err);
                }
            });
        }
    }

    public void onUpdate() {
        executor.execute(() -> {
            try {
                players = gson.fromJson(get("http://f1.rustix.me:41722/irc/online"), new TypeToken<Set<String>>() {}.getType());
                updateTHData();
            } catch (Exception e) {
                if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("IRC", Formatting.RED + e.getMessage());
                e.printStackTrace(System.err);
            }
        });
    }

    public boolean isTHUser(String name) {
        return THOnlinePlayers.contains(name);
    }

    public boolean isUser(String name) {
        return isConnected() && players.contains(name);
    }

    private boolean isConnected() {
        return players.contains(mc.getSession().getUsername());
    }
}