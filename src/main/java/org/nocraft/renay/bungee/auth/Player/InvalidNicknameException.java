package org.nocraft.renay.bungee.auth.Player;

public class InvalidNicknameException extends RuntimeException {
    public InvalidNicknameException() {
        super("Your nickname contains illegal characters\n.");
    }
}
