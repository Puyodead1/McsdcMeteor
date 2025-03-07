package com.mcsdc.addon.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.McsdcSystem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FindNewServersScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<Boolean> visitedSetting = sg.add(new BoolSetting.Builder()
        .name("visited")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> moddedSetting = sg.add(new BoolSetting.Builder()
        .name("modded")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> whitelistSetting = sg.add(new BoolSetting.Builder()
        .name("whitelist")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> crackedSetting = sg.add(new BoolSetting.Builder()
        .name("cracked")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> griefedSetting = sg.add(new BoolSetting.Builder()
        .name("griefed")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> savedSetting = sg.add(new BoolSetting.Builder()
        .name("saved")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> activeSetting = sg.add(new BoolSetting.Builder()
        .name("active")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<VersionEnum> versionSetting = sg.add(new EnumSetting.Builder<VersionEnum>()
        .name("version")
        .description("")
        .defaultValue(VersionEnum.ANY)
        .build()
    );

    WContainer settingsContainer;

    public FindNewServersScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find Servers");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("search")).expandX().widget().action = () -> {
            reload();

            CompletableFuture.supplyAsync(() -> {
                String string = "{\"search\":{\"version\":%s,\"flags\":{\"visited\":%s,\"griefed\":%s,\"modded\":%s,\"saved\":%s,\"whitelist\":%s,\"active\":%s,\"cracked\":%s}}}"
                    .formatted((versionSetting.get().number == -1) ? null : versionSetting.get().getNumber(), visitedSetting.get(), griefedSetting.get(), moddedSetting.get(), savedSetting.get(), whitelistSetting.get(), activeSetting.get(), crackedSetting.get());

                int versionNumber = (versionSetting.get().number == -1) ? null : versionSetting.get().getNumber();
                if (versionNumber == -1){
                    string = "{\"search\":{\"version\":{\"name\":\"%s\"},\"flags\":{\"visited\":false,\"griefed\":false,\"modded\":false,\"saved\":false,\"whitelist\":false,\"active\":false,\"cracked\":false}}}"
                        .formatted(versionSetting.get().getVersion(), visitedSetting.get(), griefedSetting.get(), moddedSetting.get(), savedSetting.get(), whitelistSetting.get(), activeSetting.get(), crackedSetting.get());

                }

                String response = Http.post("https://interact.mcsdc.online/api").bodyJson(string).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendString();
                return response;
            }).thenAccept(response -> {
                Map<String, String> extractedServers = extractServerInfo(response);

                add(theme.button("add all")).expandX().widget().action = () -> {
                    extractedServers.forEach((serverIP, serverVersion) -> {
                        ServerInfo info = new ServerInfo("Mcsdc " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                        multiplayerScreen.getServerList().add(info, false);
                    });
                    multiplayerScreen.getServerList().saveFile();
                    multiplayerScreen.getServerList().loadFile();
                };

                MinecraftClient.getInstance().execute(() -> {
                    WTable table = add(theme.table()).widget();

                    table.add(theme.label("Server IP"));
                    table.add(theme.label("Version"));
                    table.row();
                    table.add(theme.horizontalSeparator()).expandX();
                    table.row();

                    // Iterate through the extracted server data
                    extractedServers.forEach((serverIP, serverVersion) -> {
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

    public enum VersionEnum {
        ANY("any", -1),
        _1_21_4("1.21.4", 769),
        _1_21_2("1.21.2", 768),
        _1_21("1.21", 767),
        _1_20_5("1.20.5", 766),
        _1_20_3("1.20.3", 765),
        _1_20_2("1.20.2", 764),
        _1_20("1.20", 763),
        _1_19_4("1.19.4", 762),
        _1_19_3("1.19.3", 761),
        _1_19_2("1.19.2", 760),
        _1_19("1.19", 759),
        _1_18_2("1.18.2", 758),
        _1_18_1("1.18.1", 757),
        _1_17_1("1.17.1", 756),
        _1_17("1.17", 755),
        _1_16_5("1.16.5", 754),
        _1_16_3("1.16.3", 753),
        _1_16_2("1.16.2", 751),
        _1_16_1("1.16.1", 736),
        _1_16("1.16", 735),
        _1_15_2("1.15.2", 578),
        _1_15_1("1.15.1", 575),
        _1_15("1.15", 573),
        _1_14_4("1.14.4", 498),
        _1_14_3("1.14.3", 490),
        _1_14_2("1.14.2", 485),
        _1_14_1("1.14.1", 480),
        _1_14("1.14", 477),
        _1_13_2("1.13.2", 404),
        _1_13_1("1.13.1", 401),
        _1_13("1.13", 393),
        _1_12_2("1.12.2", 340),
        _1_12_1("1.12.1", 338),
        _1_12("1.12", 335),
        _1_11_2("1.11.2", 316),
        _1_11("1.11", 315),
        _1_10_2("1.10.2", 210),
        _1_9_4("1.9.4", 110),
        _1_9_1("1.9.1", 108),
        _1_8_9("1.8.9", 47),
        _1_7_10("1.7.10", 5),
        _1_7_5("1.7.5", 4);
        private final String version;
        private final int number;

        VersionEnum(String version, int number) {
            this.version = version;
            this.number = number;
        }

        public String getVersion() {
            return version;
        }

        public int getNumber() {
            return number;
        }
    }
}
