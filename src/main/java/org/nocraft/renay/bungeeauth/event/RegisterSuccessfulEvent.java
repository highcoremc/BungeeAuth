package org.nocraft.renay.bungeeauth.event;

import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

public class RegisterSuccessfulEvent extends Event  {

    private final UUID playerId;

    public RegisterSuccessfulEvent(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }
}
