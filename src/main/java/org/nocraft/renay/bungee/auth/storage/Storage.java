package org.nocraft.renay.bungee.auth.storage;

import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.model.user.User;
import org.nocraft.renay.bungee.auth.storage.implementation.StorageImplementation;
import org.nocraft.renay.bungee.auth.util.Throwing;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a {@link StorageImplementation}.
 */
public class Storage {
    private final BungeeAuth plugin;
    private final StorageImplementation implementation;

    public Storage(BungeeAuth plugin, StorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public StorageImplementation getImplementation() {
        return this.implementation;
    }

    public Collection<StorageImplementation> getImplementations() {
        return Collections.singleton(this.implementation);
    }

    private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getScheduler().async());
    }

    private CompletableFuture<Void> makeFuture(Throwing.Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getScheduler().async());
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

    public CompletableFuture<User> loadUser(UUID uniqueId) {
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
