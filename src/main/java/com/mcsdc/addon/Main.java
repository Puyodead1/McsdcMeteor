package com.mcsdc.addon;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import org.slf4j.Logger;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class Main extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static String mainEndpoint = "https://interact.mcsdc.online/api";
    public static Map<String, String> recentServers = new HashMap<>();

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
