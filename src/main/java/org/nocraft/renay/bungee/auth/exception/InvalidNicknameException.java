package org.nocraft.renay.bungee.auth.exception;

public class InvalidNicknameException extends RuntimeException {
    public InvalidNicknameException() {
        super("Your nickname contains illegal characters\n.");
    }
}
