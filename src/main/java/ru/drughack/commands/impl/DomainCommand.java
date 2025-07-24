package ru.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import ru.drughack.commands.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class DomainCommand extends Command {

    public DomainCommand() {
        super("domain <drughack.cc>", "Domain", "check the domain", "domain", "domen");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(arg("domain", word())
                        .executes(context -> {
                            String domain = context.getArgument("domain", String.class);
                            checkDomain(domain);
                            return SINGLE_SUCCESS;
                        })
                );
    }

    private void checkDomain(String domain) {
        try {
            URL url = new URL("https://api.bp-project.net/domains_availability?reg_type=Basic");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer zalupa");
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = String.format("{\"domains\": [\"%s\"]}", domain);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String responseBody;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(responseCode < 400 ? connection.getInputStream() : connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) response.append(responseLine.trim());
                responseBody = response.toString();
            }

            switch (responseCode) {
                case 200 -> sendMessage("Success! Domain: '" + domain + "' available.");
                case 422 -> sendMessage("Error: Domain '" + domain + "' already busy or invalided.");
                default -> sendMessage("Unexpected answer (" + responseCode + "): " + responseBody);
            }

            connection.disconnect();

        } catch (Exception ignored) {}
    }
}