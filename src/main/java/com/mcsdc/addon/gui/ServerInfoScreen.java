package com.mcsdc.addon.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.McsdcSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.utils.network.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ServerInfoScreen extends WindowScreen {

    private final String ip;

    public ServerInfoScreen(String ip) {
        super(GuiThemes.get(), "ServerInfo");
        this.ip = ip;
    }

    @Override
    public void initWidgets() {
        CompletableFuture.supplyAsync(() -> {
            String string = "{\"search\":{\"address\":\"%s\"}}"
                .formatted(this.ip);

            Main.LOG.info(string); // i dont know why the fuck this request does not want to work at all, i am going to kill myself
            String response = Http.post("https://interact.mcsdc.online/api").bodyString(string).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendString();

            Main.LOG.info(response);
            return response;
        }).thenAccept(response -> {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;

            try {
                jsonNode = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                Main.LOG.error("Error parsing response as JSON", e);
                return;
            }


            WTable table = add(theme.table()).widget();

            table.add(theme.label("version: %s".formatted(jsonNode.get("version").asText())));table.row();
            table.add(theme.label("visited: %s".formatted(jsonNode.get("status").get("visited").asText())));table.row();
            table.add(theme.label("griefed: %s".formatted(jsonNode.get("status").get("griefed").asText())));table.row();
            table.add(theme.label("modded: %s".formatted(jsonNode.get("status").get("modded").asText())));table.row();
            table.add(theme.label("whitelist: %s".formatted(jsonNode.get("status").get("whitelist").asText())));table.row();
            table.add(theme.label("banned: %s".formatted(jsonNode.get("status").get("banned").asText())));table.row();
            table.add(theme.label("save_for_later: %s".formatted(jsonNode.get("status").get("save_for_later").asText())));table.row();

            table.add(theme.label("notes: %s".formatted(jsonNode.get("notes").asText())));table.row();
            table.add(theme.label("last_seen_online: %s".formatted(jsonNode.get("last_seen_online").asText())));table.row();
            table.add(theme.label("last_scanned: %s".formatted(jsonNode.get("last_scanned").asText()))); table.row();
            table.add(theme.label("last_joined: %s".formatted(jsonNode.get("last_joined").asText()))); table.row();
            table.add(theme.label("historical: %s".formatted(jsonNode.get("historical").asText()))); table.row();
        });
    }

    public static String sendPostRequest(String urlString, String jsonBody, String authorizationToken) throws IOException {
        // Create a URL object from the given URL string
        URL url = new URL(urlString);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the HTTP request method to POST
        connection.setRequestMethod("POST");

        // Set the content-type to JSON
        connection.setRequestProperty("Content-Type", "application/json");

        // Set the Authorization header with the provided token (e.g., Bearer token)
        connection.setRequestProperty("Authorization", "Bearer " + authorizationToken);

        // Enable input/output streams for the connection
        connection.setDoOutput(true);

        // Write the JSON body to the output stream
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Get the response code from the server
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the JSON response from the server if the response code is 200 (OK)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("JSON Response Body: " + response.toString());
                return response.toString();
            }
        } else {
            System.out.println("Request failed. Response Code: " + responseCode);
        }

        return null;
    }
}
