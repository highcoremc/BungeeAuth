package org.nocraft.renay.bungeeauth.storage.data;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.AbstractStorage;
import org.nocraft.renay.bungeeauth.storage.entity.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a {@link DataStorage}.
 */
public class SimpleDataStorage extends AbstractStorage {
    private final BungeeAuthPlugin plugin;
    private final DataStorage implementation;

    public SimpleDataStorage(BungeeAuthPlugin plugin, DataStorage implementation) {
        super(plugin);
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public DataStorage getImplementation() {
        return this.implementation;
    }

    public Collection<DataStorage> getImplementations() {
        return Collections.singleton(this.implementation);
    }

    public String getName() {
        return this.implementation.getImplementationName();
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to shutdown storage implementation");
            e.printStackTrace();
        }
    }

    public CompletableFuture<Optional<User>> loadUser(UUID uniqueId) {
        return makeFuture(() -> this.implementation.loadUser(uniqueId));
    }

    public CompletableFuture<Void> saveUser(User user) {
        return makeFuture(() -> this.implementation.saveUser(user));
    }

    public CompletableFuture<Set<UUID>> getUniqueUsers() {
        return makeFuture(this.implementation::getUniqueUsers);
    }

    public CompletableFuture<UUID> getPlayerUniqueId(String username) {
        return makeFuture(() -> this.implementation.getPlayerUniqueId(username));
    }

    public CompletableFuture<String> getPlayerName(UUID uniqueId) {
        return makeFuture(() -> this.implementation.getPlayerName(uniqueId));
    }
}
