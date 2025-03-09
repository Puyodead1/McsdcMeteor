package com.mcsdc.addon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerSearchBuilder;
import com.mcsdc.addon.system.ServerStorage;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FindNewServersScreen extends WindowScreen {
    private static FindNewServersScreen instance = null;
    private MultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();
    private boolean searching = false;

    private final Setting<Flags> visitedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("visited")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> moddedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("modded")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> whitelistSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("whitelist")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> crackedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("cracked")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> griefedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("griefed")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> savedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("saved")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> activeSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("active")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Boolean> vanilla = sg.add(new BoolSetting.Builder()
        .name("vanilla")
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

    public static FindNewServersScreen instance(MultiplayerScreen multiplayerScreen, Screen parent) {
        if (instance == null) {
            instance = new FindNewServersScreen();
        }
        instance.setMultiplayerScreen(multiplayerScreen);
        instance.setParent(parent);
        return instance;
    }

    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public FindNewServersScreen() {
        super(GuiThemes.get(), "Find Servers");
    }

    List<ServerStorage> extractedServers;
    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("search")).expandX().widget().action = () -> {
            if (searching) return;
            reload();

            if (visitedSetting.get().bool == null && griefedSetting.get().bool  == null && moddedSetting.get().bool  == null && savedSetting.get().bool  == null && whitelistSetting.get().bool  == null && activeSetting.get().bool == null && crackedSetting.get().bool  == null &&
                versionSetting.get().number == -1){
                add(theme.label("Everything searches are not allowed.")).expandX().widget();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                searching = true;
                add(theme.label("Searching...")).expandX().widget();

                // Example 1: Version is a string
                Object ver;
                int number = versionSetting.get().number;
                boolean isVanilla = vanilla.get();

                if (isVanilla && number != -1) {
                    ver = versionSetting.get().version; // Use the string version
                } else if (number != -1) {
                    ver = number; // Use protocol number if valid
                } else {
                    ver = null; // Set to null only if explicitly invalid
                }

                ServerSearchBuilder.Version versionString = new ServerSearchBuilder.Version(ver);
                ServerSearchBuilder.Flags flags = new ServerSearchBuilder.Flags(visitedSetting.get().bool, griefedSetting.get().bool, moddedSetting.get().bool, savedSetting.get().bool, whitelistSetting.get().bool, activeSetting.get().bool, crackedSetting.get().bool);
                ServerSearchBuilder.Search searchString = new ServerSearchBuilder.Search(versionString, flags);

                JsonObject jsonString = ServerSearchBuilder.createJson(searchString);

                return Http.post(Main.mainEndpoint).bodyString(jsonString.toString()).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendStringResponse().body();
            }).thenAccept(response -> {
                searching = false;
                if (response == null || extractedServers.isEmpty()){
                    add(theme.label("No servers found.")).expandX().widget();
                    return;
                }

                if (response.endsWith(",]")){ // depending on the search, response can be slightly malformed.
                    response = response.substring(0, response.length() - 2) + "]";
                }

                extractedServers = extractServerInfo(response);

                MinecraftClient.getInstance().execute(() -> {
                    WHorizontalList buttons = add(theme.horizontalList()).expandX().widget();
                    WTable table = add(theme.table()).widget();
                    buttons.add(theme.button("add all")).expandX().widget().action = () -> {
                        extractedServers.forEach((server) -> {
                            ServerInfo info = new ServerInfo("Mcsdc " + server.ip, server.ip, ServerInfo.ServerType.OTHER);
                            multiplayerScreen.getServerList().add(info, false);
                        });
                        multiplayerScreen.getServerList().saveFile();
                        multiplayerScreen.getServerList().loadFile();
                    };

                    buttons.add(theme.button("randomize")).expandX().widget().action = () -> {
                        Collections.shuffle(extractedServers);

                        generateWidgets(extractedServers, table);
                    };

                    generateWidgets(extractedServers, table);
                });

            });
        };
    }

    public void generateWidgets(List<ServerStorage> extractedServers, final WTable table){
        MinecraftClient.getInstance().execute(() -> {
            table.clear();

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
    }

    public static List<ServerStorage> extractServerInfo(String jsonResponse) {
        List<ServerStorage> serverStorageList = new ArrayList<>();
        JsonArray jsonObject = JsonParser.parseString(jsonResponse).getAsJsonArray();

        jsonObject.forEach(node -> {
            String address = node.getAsJsonObject().get("address").getAsString();
            String version = node.getAsJsonObject().get("version").getAsString();
            serverStorageList.add(new ServerStorage(address, version));
        });

        return serverStorageList;
    }

    public enum Flags{
        YES(true),
        NO(false),
        ANY(null);

        Boolean bool;
        Flags(Boolean bool){
            this.bool =bool;
        }
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

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
