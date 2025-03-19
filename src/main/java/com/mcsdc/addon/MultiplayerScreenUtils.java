package com.mcsdc.addon;

import com.mcsdc.addon.mixin.MultiplayerScreenAccessor;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class MultiplayerScreenUtils {
    public static void save(MultiplayerScreen multiplayerScreen) {
        multiplayerScreen.getServerList().saveFile();
    }

    public static void reload(MultiplayerScreen multiplayerScreen){
        MultiplayerScreenAccessor msa = getAccessor(multiplayerScreen);
        msa.getServerListWidget().setServers(multiplayerScreen.getServerList());
    }


    public static MultiplayerScreenAccessor getAccessor(MultiplayerScreen multiplayerScreen){
        return (MultiplayerScreenAccessor) multiplayerScreen;
    }
}
