package org.nocraft.renay.bungeeauth.authentication;

public interface Authentication {
    enum Result {
        SUCCESS_LOGIN,
        WRONG_PASSWORD,
        ACCOUNT_NOT_FOUND,
        ALREADY_AUTHENTICATED
    }
}
