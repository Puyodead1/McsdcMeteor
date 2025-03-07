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
}
