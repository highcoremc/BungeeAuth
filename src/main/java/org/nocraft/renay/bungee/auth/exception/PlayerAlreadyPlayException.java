package org.nocraft.renay.bungee.auth.exception;

public class PlayerAlreadyPlayException extends RuntimeException {
    public PlayerAlreadyPlayException() {
        super("Player with the same name already play on this server.");
    }
}
