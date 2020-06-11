package org.nocraft.renay.bungeeauth.event;

import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

public class PlayerAttemptsLoginExceeded extends Event {

    private final UUID uniqueId;

    public PlayerAttemptsLoginExceeded(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getPlayerId() {
        return uniqueId;
    }
}
