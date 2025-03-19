package com.mcsdc.addon.system;

import javax.annotation.Nullable;

public record ServerStorage(String ip, String version, @Nullable Long lastScanned, @Nullable Long lastSeen) {

}
