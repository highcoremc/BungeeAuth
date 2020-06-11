package org.nocraft.renay.bungeeauth.event;

import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

public class PlayerSuccessfulLoginEvent extends Event {

    private final UUID playerId;

    public PlayerSuccessfulLoginEvent(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }
}
