package me.loper.bungeeauth.storage.session;

import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.storage.AbstractStorageAdapter;
import me.loper.storage.Storage;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SimpleSessionStorage extends AbstractStorageAdapter {

    private final SessionStorage implementation;
    private final BungeeAuthPlugin plugin;

    private boolean isLoaded = false;

    public SimpleSessionStorage(BungeeAuthPlugin plugin, SessionStorage implementation) {
        super(plugin.getScheduler());
        this.implementation = implementation;
        this.plugin = plugin;
    }

    @Override
    public Storage getImplementation() {
        return this.implementation;
    }

    public void init() {
        try {
            this.implementation.init();
            this.isLoaded = true;
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation");
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    public CompletableFuture<Optional<Session>> loadSession(UUID uniqueId, String key) {
        return makeFuture(() -> this.implementation.loadSession(uniqueId, key));
    }

    public CompletableFuture<Map<String, Session>> loadSessions(UUID uniqueId) {
        return makeFuture(() -> this.implementation.loadSessions(uniqueId));
    }

    public CompletableFuture<Void> save(Session session) {
        return makeFuture(() -> this.implementation.saveSession(session));
    }

    @Override
    public void shutdown() {
        if (!this.isLoaded) {
            return;
        }

        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation");
            e.printStackTrace();
        }
    }

    public CompletableFuture<Queue<Map<String, Session>>> loadAll() {
        return makeFuture(this.implementation::loadAllSessions);
    }

    public CompletableFuture<Void> remove(UUID uniqueId, String key) {
        return makeFuture(() -> this.implementation.removeSession(uniqueId, key));
    }

    public CompletableFuture<Void> remove(List<Session> sessions) {
        return makeFuture(() -> this.implementation.removeSession(sessions));
    }
}
