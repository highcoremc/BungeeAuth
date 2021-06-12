package me.loper.bungeeauth.exception;

import java.util.UUID;

public class AuthenticationException extends RuntimeException {
    private final String playerName;
    private final UUID uniqueId;

    public AuthenticationException(String playerName, UUID uniqueId, Exception previousEx) {
        super(String.format("Player %s with unique id %s was not authenticated.", playerName, uniqueId.toString()), previousEx);
        this.playerName = playerName;
        this.uniqueId = uniqueId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
}
