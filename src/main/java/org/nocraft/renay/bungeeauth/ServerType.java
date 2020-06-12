package org.nocraft.renay.bungeeauth;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum ServerType {
    LOGIN("login"),
    GAME("game"),
    UNKNOWN("unknown");

    private final String type;

    ServerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
