package org.nocraft.renay.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;


public class User {

    private final UUID uniqueId;
    private final String username;
    private final String realname;

    private final String registeredIp;
    private final long registeredAt;

    private String lastSeenIp;
    private long lastSeen;

    public User(UUID uniqueId, String username, String registeredIp) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.registeredIp = registeredIp;
        this.realname = username.toLowerCase();
        this.registeredAt = System.currentTimeMillis();
        this.updateLastSeen(registeredIp);
    }

    public @NonNull UUID getUniqueId() {
        return this.uniqueId;
    }

    public @NonNull String getName() {
        return this.username;
    }

    public long getRegisteredAt() {
        return this.registeredAt;
    }

    public String getRegisteredIp() {
        return this.registeredIp;
    }

    public long getLastSeen() {
        return this.lastSeen;
    }

    public String getLastSeenIp() {
        return this.lastSeenIp;
    }

    public void updateLastSeen(String ip) {
        this.lastSeen = System.currentTimeMillis();
        this.lastSeenIp = ip;
    }

    public String getRealname() {
        return realname;
    }
}
