package com.mcsdc.addon.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcsdc.addon.system.McsdcSystem;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FindPlayerScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> playerSetting = sg.add(new StringSetting.Builder()
        .name("name/uuid")
        .description("")
        .defaultValue("Notch")
        .build()
    );

    public FindPlayerScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find Player");
        this.multiplayerScreen = multiplayerScreen;
    }
    WContainer settingsContainer;
    @Override
    public void initWidgets() {

        WContainer settingsContainer = add(theme.verticalList()).expandX().widget();
        settingsContainer.minWidth = 300;
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("search")).expandX().widget().action = () -> {
            reload();

            CompletableFuture.supplyAsync(() -> {
                String string = "{\"search\":{\"player\":\"%s\"}}"
                    .formatted(this.playerSetting.get());

                String response = Http.post("https://interact.mcsdc.online/api").bodyJson(string).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendString();
                return response;

            }).thenAccept(response -> {
                MinecraftClient.getInstance().execute(() -> {
                    WTable table = add(theme.table()).widget();

                    table.add(theme.label("Server IP"));
                    table.add(theme.label("Version"));
                    table.row();
                    table.add(theme.horizontalSeparator()).expandX();
                    table.row();

                    // Iterate through the extracted server data
                    extractServerInfo(response).forEach((serverIP, serverVersion) -> {
                        table.add(theme.label(serverIP));
                        table.add(theme.label(serverVersion));

                        WButton addServerButton = theme.button("Add Server");
                        addServerButton.action = () -> {
                            ServerInfo info = new ServerInfo("Mcsdc " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                            multiplayerScreen.getServerList().add(info, false);
                            multiplayerScreen.getServerList().saveFile();
                            multiplayerScreen.getServerList().loadFile();
                            addServerButton.visible = false;
                        };

                        WButton joinServerButton = theme.button("Join Server");
                        joinServerButton.action = () ->
                            ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(),
                                new ServerAddress(serverIP.split(":")[0], Integer.parseInt(serverIP.split(":")[1])),
                                new ServerInfo("a", serverIP, ServerInfo.ServerType.OTHER), false, null);

                        WButton serverInfoButton = theme.button("Server Info");
                        serverInfoButton.action = () -> {
                            MinecraftClient.getInstance().setScreen(new ServerInfoScreen(serverIP));
                        };

                        table.add(addServerButton);
                        table.add(joinServerButton);
                        table.add(serverInfoButton);
                        table.row();
                    });
                });
            });
        };
    }

    public static Map<String, String> extractServerInfo(String jsonResponse) {
        Map<String, String> serverInfo = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse JSON array
            List<JsonNode> servers = objectMapper.readValue(jsonResponse, new TypeReference<List<JsonNode>>() {});

            // Loop through each server object and extract "address" and "version"
            for (JsonNode server : servers) {
                String address = server.get("address").asText();
                String version = server.get("version").asText();
                serverInfo.put(address, version);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverInfo;
    }
}
