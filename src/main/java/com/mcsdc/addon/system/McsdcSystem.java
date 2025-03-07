package com.mcsdc.addon.system;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;

public class McsdcSystem extends System<McsdcSystem> {
    private String token = "";
    private String username = "";
    private int level = -1;

    public McsdcSystem() {
        super("McsdcSystem");
    }

    public static McsdcSystem get() {
        return Systems.get(McsdcSystem.class);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound compound = new NbtCompound();
        compound.putString("token", this.token);
        compound.putString("username", this.username);
        compound.putInt("level", this.level);

        return compound;
    }

    @Override
    public McsdcSystem fromTag(NbtCompound tag) {
        this.token = tag.getString("token");
        this.username = tag.getString("username");
        this.level = tag.getInt("level");

        return super.fromTag(tag);
    }
}
