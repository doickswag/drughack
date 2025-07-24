package ru.drughack.api.protection;

import lombok.Getter;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.modules.api.Module;
import ru.drughack.utils.interfaces.Wrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class IVC implements Wrapper {

    private WebSocket socket;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public IVC() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::onDisconnect));
    }

    public void onConnect() {
        executor.execute(() -> {
            try {
                WebSocket.Builder builder = client.newWebSocketBuilder();
                builder.header("Authorization", "Bearer " + DrugHack.getInstance().getProtection().getToken());

                socket = builder.buildAsync(URI.create("ws://f1.rustix.me:41722/irc/chat"), new WebSocketListener()).join();
            } catch (Exception e) {
                if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("Chat", Formatting.RED + e.getMessage());
            }
        });
    }

    public void onDisconnect() {
        executor.execute(() -> {
            try {
                if (socket != null) socket.sendClose(WebSocket.NORMAL_CLOSURE, "disconnected");
            } catch (Exception e) {
                if (!Module.fullNullCheck()) DrugHack.getInstance().getChatManager().message("Chat", Formatting.RED + e.getMessage());
            }
        });
    }

    public static class WebSocketListener implements WebSocket.Listener, Wrapper {

        @Override
        public void onOpen(WebSocket webSocket) {
            if (!Module.fullNullCheck() && DrugHack.getInstance().getModuleManager().getIrc().chat.getValue().isEnabled()) DrugHack.getInstance().getChatManager().message("Chat", Formatting.GREEN + "Successfully connected to the chat");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            if (!Module.fullNullCheck() && DrugHack.getInstance().getModuleManager().getIrc().chat.getValue().isEnabled()) DrugHack.getInstance().getChatManager().message("Chat", data.toString());
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            if (!Module.fullNullCheck() && DrugHack.getInstance().getModuleManager().getIrc().chat.getValue().isEnabled()) {
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                DrugHack.getInstance().getChatManager().message("Chat", bytesToHex(bytes));
            }

            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (!Module.fullNullCheck() && DrugHack.getInstance().getModuleManager().getIrc().chat.getValue().isEnabled()) {
                if (statusCode == WebSocket.NORMAL_CLOSURE) DrugHack.getInstance().getChatManager().message("Chat", Formatting.RED + "Successfully disconnected from chat");
                else DrugHack.getInstance().getChatManager().message("Chat", Formatting.RED + "Failed disconnect from the chat: " + statusCode);
            }

            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (!Module.fullNullCheck() && DrugHack.getInstance().getModuleManager().getIrc().chat.getValue().isEnabled()) DrugHack.getInstance().getChatManager().message("Chat", Formatting.RED + "Connection failed: " + error.getMessage());
            WebSocket.Listener.super.onError(webSocket, error);
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));

            return sb.toString();
        }
    }
}