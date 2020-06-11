package org.nocraft.renay.bungeeauth.storage.entity;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.AbstractStorage;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.storage.session.SessionStorage;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimpleSessionStorage extends AbstractStorage {

    private final SessionStorage implementation;
    private final BungeeAuthPlugin plugin;

    public SimpleSessionStorage(BungeeAuthPlugin plugin, SessionStorage implementation) {
        super(plugin);
        this.implementation = implementation;
        this.plugin = plugin;
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation");
            e.printStackTrace();
        }
    }

    public CompletableFuture<Optional<Session>> loadSession(UUID uniqueId, String key) {
        return makeFuture(() -> this.implementation.loadSession(uniqueId, key));
    }

    public CompletableFuture<Void> save(Session session) {
        return makeFuture(() -> this.implementation.saveSession(session));
    }

    @Override
    public void shutdown() {
        this.implementation.shutdown();
    }

    public CompletableFuture<Queue<Map<String, Session>>> loadAll() {
        return makeFuture(this.implementation::loadAllSessions);
    }

    public CompletableFuture<Void> remove(UUID uniqueId, String key) {
        return makeFuture(() -> this.implementation.removeSession(uniqueId, key));
    };
}
