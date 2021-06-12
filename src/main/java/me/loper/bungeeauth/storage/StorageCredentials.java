package me.loper.bungeeauth.storage;

public interface StorageCredentials {

    String getAddress();

    String getDatabase();

    String getUsername();

    String getPassword();

    int getConnectionTimeout();

    int getMaxPoolSize();
}
