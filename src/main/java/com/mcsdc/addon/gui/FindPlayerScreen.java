package com.mcsdc.addon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.FindPlayerSearchBuilder;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FindPlayerScreen extends WindowScreen {
    private static FindPlayerScreen instance = null;
    private MultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> playerSetting = sg.add(new StringSetting.Builder()
        .name("name/uuid")
        .description("")
        .defaultValue("popbob")
        .build()
    );

    public static FindPlayerScreen instance(MultiplayerScreen multiplayerScreen, Screen parent) {
        if (instance == null) {
            instance = new FindPlayerScreen();
        }
        instance.setMultiplayerScreen(multiplayerScreen);
        instance.setParent(parent);
        return instance;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public MultiplayerScreen setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        return this.multiplayerScreen = multiplayerScreen;
    }

    public FindPlayerScreen() {
        super(GuiThemes.get(), "Find Player");
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
            if (playerSetting.get().isEmpty()){
                add(theme.label("Enter a name/uuid")).expandX();
            }

            CompletableFuture.supplyAsync(() -> {
                JsonObject toSearch = FindPlayerSearchBuilder.create(playerSetting.get());
                return Http.post(Main.mainEndpoint).bodyJson(toSearch).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendStringResponse().body();
            }).thenAccept(response -> {
                List<ServerStorage> extractedServers = extractServerInfo(response);
                if (extractedServers.isEmpty() || response == null){
                    add(theme.label("No servers found."));
                    return;
                }

                add(theme.button("add all")).expandX().widget().action = () -> {
                    extractedServers.forEach((server) -> {
                        ServerInfo info = new ServerInfo("Mcsdc " + server.ip, server.ip, ServerInfo.ServerType.OTHER);
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
                    extractedServers.forEach((server) -> {
                        String serverIP = server.ip;
                        String serverVersion = server.version;

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

    public static List<ServerStorage> extractServerInfo(String jsonResponse) {
        List<ServerStorage> serverStorageList = new ArrayList<>();
        JsonArray array = JsonParser.parseString(jsonResponse).getAsJsonArray();

        array.forEach(node -> {
            String address = node.getAsJsonObject().get("address").getAsString();
            String version = node.getAsJsonObject().get("version").getAsString();
            serverStorageList.add(new ServerStorage(address, version));
        });

        return serverStorageList;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
