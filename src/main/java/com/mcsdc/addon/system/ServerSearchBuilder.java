package com.mcsdc.addon.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ServerSearchBuilder {
    public static class Version {
        Object value;

        public Version(Object value) {
            this.value = value;
        }

        public JsonElement toJson() {
            if (value == null) {
                return null;
            }
            JsonObject versionObject = new JsonObject();
            if (value instanceof Integer) {
                versionObject.addProperty("protocol", (Integer) value);
            } else {
                versionObject.addProperty("name", value.toString());
            }
            return versionObject;
        }
    }

    public static class Flags {
        Boolean visited, griefed, modded, saved, whitelist, active, cracked;

        public Flags(Boolean visited, Boolean griefed, Boolean modded, Boolean saved,
                     Boolean whitelist, Boolean active, Boolean cracked) {
            this.visited = visited;
            this.griefed = griefed;
            this.modded = modded;
            this.saved = saved;
            this.whitelist = whitelist;
            this.active = active;
            this.cracked = cracked;
        }

        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            if (visited != null) jsonObject.addProperty("visited", visited);
            if (griefed != null) jsonObject.addProperty("griefed", griefed);
            if (modded != null) jsonObject.addProperty("modded", modded);
            if (saved != null) jsonObject.addProperty("saved", saved);
            if (whitelist != null) jsonObject.addProperty("whitelist", whitelist);
            if (active != null) jsonObject.addProperty("active", active);
            if (cracked != null) jsonObject.addProperty("cracked", cracked);
            return jsonObject;
        }
    }

    public static class Search {
        Version version;
        Flags flags;

        public Search(Version version, Flags flags) {
            this.version = version;
            this.flags = flags;
        }
    }

    public static JsonObject createJson(Search search) {
        JsonObject rootJson = new JsonObject(), searchJson = new JsonObject();

        // Handle version dynamically (null, int -> protocol, or string -> name)
        if (search.version != null) {
            JsonElement versionElement = search.version.toJson();
            searchJson.add("version", versionElement);
        } else {
            searchJson.add("version", null);
        }

        // Ensure "flags" always exists but only contains non-null fields
        JsonObject flagsJson = search.flags != null ? search.flags.toJsonObject() : new JsonObject();
        searchJson.add("flags", flagsJson);

        rootJson.add("search", searchJson);
        return rootJson;
    }
}

