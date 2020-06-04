package org.nocraft.renay.bungeeauth.user;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String username;
    private Session activeSession;

    public User(UUID uniqueId, String username) {
        this.id = uniqueId;
        this.username = username;
    }

    public void changeActiveSession(Session session) {
        this.activeSession = session;
    }

    public @NonNull UUID userId() {
        return this.id;
    }

    public @NonNull String userName() {
        return this.username;
    }

    public @NonNull Session activeSession() {
        return this.activeSession;
    }
}
