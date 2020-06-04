package org.nocraft.renay.bungeeauth.exception;

public class InvalidNicknameException extends RuntimeException {
    public InvalidNicknameException() {
        super("Your nickname contains illegal characters\n.");
    }
}
