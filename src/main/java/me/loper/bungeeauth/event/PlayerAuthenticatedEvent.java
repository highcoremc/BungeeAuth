package me.loper.bungeeauth.event;

import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

public class PlayerAuthenticatedEvent extends Event {

    private final UUID playerId;
    private final boolean connectHandled;

    public PlayerAuthenticatedEvent(UUID playerId) {
        this.playerId = playerId;
        this.connectHandled = false;
    }

    public PlayerAuthenticatedEvent(UUID playerId, boolean connectHandled) {
        this.playerId = playerId;
        this.connectHandled = connectHandled;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public boolean isConnectHandled() {
        return this.connectHandled;
    }
}
