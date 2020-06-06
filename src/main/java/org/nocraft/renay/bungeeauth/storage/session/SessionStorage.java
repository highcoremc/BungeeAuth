package org.nocraft.renay.bungeeauth.storage.session;

import org.nocraft.renay.bungeeauth.storage.Storage;

import java.util.Optional;
import java.util.UUID;

public interface SessionStorage extends Storage {

    Optional<Session> loadSession(UUID uniqueId, String key);

    void saveSession(Session session);

}
