package com.mcsdc.addon.gui;

import com.mcsdc.addon.system.McsdcSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;


public class McsdcScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public McsdcScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Mcsdc");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        String authToken = McsdcSystem.get().getToken();

        if (authToken.isEmpty()) {
            this.client.setScreen(new LoginScreen(this));
            return;
        }

        WHorizontalList accountList = add(theme.horizontalList()).expandX().widget();
        accountList.add(theme.label("")).expandX();
        accountList.add(theme.label("User: " + McsdcSystem.get().getUsername())).expandX();
        accountList.add(theme.label("Perms: " + McsdcSystem.get().getLevel())).expandX();

        WButton logoutButton = accountList.add(theme.button("Logout")).widget();
        logoutButton.action = () -> {
            McsdcSystem.get().setToken("");
            McsdcSystem.get().setUsername("");
            McsdcSystem.get().setLevel(-1);
            reload();
        };

        WHorizontalList widgetList = add(theme.horizontalList()).expandX().widget();
        WButton newServersButton = widgetList.add(this.theme.button("Find new servers")).expandX().widget();
        WButton findPlayersButton = widgetList.add(this.theme.button("Search players")).expandX().widget();
        WButton removeServersButton = widgetList.add(this.theme.button("Remove Servers")).expandX().widget();

        newServersButton.action = () -> {
            this.client.setScreen(new FindNewServersScreen(this.multiplayerScreen));
        };

        findPlayersButton.action = () -> {
            this.client.setScreen(new FindPlayerScreen(this.multiplayerScreen));
        };

        removeServersButton.action = () -> {
            for (int i = 0; i < this.multiplayerScreen.getServerList().size(); i++) {
                if (this.multiplayerScreen.getServerList().get(i).name.startsWith("Mcsdc")) {
                    this.multiplayerScreen.getServerList().remove(this.multiplayerScreen.getServerList().get(i));
                    i--;
                }
            }

            multiplayerScreen.getServerList().saveFile();
            multiplayerScreen.getServerList().loadFile();
        };

//        findPlayersButton.action = () -> {
//            if (this.client == null) return;
//            this.client.setScreen(new FindPlayerScreen(this.multiplayerScreen));
//        };
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(null));
    }
}
