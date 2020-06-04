package org.nocraft.renay.bungeeauth.storage.misc;

import org.nocraft.renay.bungeeauth.storage.StorageCredentials;

public class CacheStorageCredentials implements StorageCredentials {

    private final String address;
    private final String database;
    private final String username;
    private final String password;
    private final int timeout;

    public CacheStorageCredentials(String address, String database, String username, String password, int timeout) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getConnectionTimeout() {
        return this.timeout;
    }
}
