package com.mcsdc.addon;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

public class Main extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static String mainEndpoint = "https://interact.mcsdc.online/api";
    public static MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");
    }

    @Override
    public String getPackage() {
        return "com.mcsdc.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
