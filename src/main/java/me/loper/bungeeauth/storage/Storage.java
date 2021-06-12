package me.loper.bungeeauth.storage;

public interface Storage {
    String getImplementationName();

    void init() throws Exception;

    void shutdown();
}
