package org.nocraft.renay.bungee.auth.storage.implementation;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.model.user.User;

import java.util.*;

public interface StorageImplementation {
    BungeeAuth getPlugin();

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    User loadUser(UUID uniqueId) throws Exception;

    void saveUser(User user) throws Exception;

    Set<UUID> getUniqueUsers() throws Exception;

    @Nullable UUID getPlayerUniqueId(String username) throws Exception;

    @Nullable String getPlayerName(UUID uniqueId) throws Exception;
}
