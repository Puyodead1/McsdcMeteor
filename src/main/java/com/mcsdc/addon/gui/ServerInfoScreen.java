package com.mcsdc.addon.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.McsdcSystem;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.utils.network.Http;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

public class ServerInfoScreen extends WindowScreen {

    private final String ip;

    public ServerInfoScreen(String ip) {
        super(GuiThemes.get(), "ServerInfo");
        this.ip = ip;
    }

    @Override
    public void initWidgets() {
        CompletableFuture.supplyAsync(() -> {
            String string =
                "{\"search\":{\"address\":\"%s\"}}".formatted(this.ip);

            HttpResponse<String> response = Http.post(
                Main.mainEndpoint
            )
                .bodyString(string)
                .header(
                    "authorization",
                    "Bearer " + McsdcSystem.get().getToken()
                )
                .sendStringResponse();

            return response.body();
        }).thenAccept(response -> {
            MinecraftClient.getInstance().execute(() -> {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

                WTable table = add(theme.table()).widget();

                table.add(theme.horizontalSeparator("Info")).expandX().widget();
                table.row();

                table.add(
                    theme.label("version: %s".formatted(jsonObject.get("version").getAsString()))
                );
                table.row();

                table.add(theme.horizontalSeparator("Status")).expandX().widget();
                table.row();

                table.add(
                    theme.label("visited: %s".formatted(jsonObject.get("status").getAsJsonObject().get("visited").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("griefed: %s".formatted(jsonObject.get("status").getAsJsonObject().get("griefed").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("modded: %s".formatted(jsonObject.get("status").getAsJsonObject().get("modded").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("whitelist: %s".formatted(jsonObject.get("status").getAsJsonObject().get("whitelist").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("banned: %s".formatted(jsonObject.get("status").getAsJsonObject().get("banned").getAsString()))
                );
                table.row();
                table.add(
                    theme.label(
                        "save for later: %s".formatted(
                            jsonObject
                                .get("status").getAsJsonObject()
                                .get("save_for_later")
                                .getAsString()
                        )
                    )
                );
                table.row();

//            String notes = jsonObject.get("notes").getAsString();
//            Main.LOG.info(notes);
//            if (notes != null && !notes.isEmpty()){
//                table.add(
//                    theme.label("notes: %s".formatted(notes))
//                );
//            }

                table.add(theme.horizontalSeparator("Scanned")).expandX().widget();
                table.row();

                table.row();
                table.add(
                    theme.label("last seen online: %s".formatted(timeAgo(jsonObject.get("last_seen_online").getAsLong())))
                );
                table.row();
                table.add(
                    theme.label("last scanned: %s".formatted(timeAgo(jsonObject.get("last_scanned").getAsLong())))
                );
                table.row();
                table.add(
                    theme.label("last joined: %s".formatted(timeAgo(jsonObject.get("last_joined").getAsLong()))));
                table.row();

                WTable accounts = add(theme.table()).expandX().widget();
                accounts.add(theme.horizontalSeparator("Historical")).expandX().widget();
                accounts.row();

                JsonArray array = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("historical");
                if (array.isEmpty()) accounts.add(theme.label("No historical players found."));
                else {
                    for (JsonElement jsonElement : array) {
                        String name = jsonElement.getAsJsonObject().get("name").getAsString();
                        Main.LOG.info(name);
                        accounts.add(theme.label(name)).expandX().widget();
                        accounts.add(theme.button("Login")).expandX().widget().action = () -> {
                            new CrackedAccount(name).login();
                        };
                        if (array.asList().getLast() != jsonElement) accounts.row();
                    }
                }
            });
        });
    }

    public static String timeAgo(long timestampMillis) {
        long currentMillis = System.currentTimeMillis();
        long diffMillis = currentMillis - timestampMillis;

        if (diffMillis < 0) {
            return "In the future"; // Handles cases where timestamp is in the future
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
        if (seconds < 60) {
            return seconds + " seconds ago";
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        return hours + " hours ago";
    }





}
