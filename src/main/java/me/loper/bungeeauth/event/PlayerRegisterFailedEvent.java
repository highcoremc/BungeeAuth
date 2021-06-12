package me.loper.bungeeauth.event;

import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

public class PlayerRegisterFailedEvent extends Event {

    private final UUID playerId;

    public PlayerRegisterFailedEvent(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}
