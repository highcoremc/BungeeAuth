package org.nocraft.renay.bungeeauth.storage.data;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.nocraft.renay.bungeeauth.storage.Storage;
import org.nocraft.renay.bungeeauth.storage.entity.User;

import java.util.*;

public interface DataStorage extends Storage {
    Optional<User> loadUser(UUID uniqueId) throws Exception;

    void saveUser(User user) throws Exception;

    Set<UUID> getUniqueUsers() throws Exception;

    @Nullable UUID getPlayerUniqueId(String username) throws Exception;

    @Nullable String getPlayerName(UUID uniqueId) throws Exception;
}
