package org.nocraft.renay.bungeeauth.storage.session;

import com.sun.jmx.remote.internal.ArrayQueue;
import org.nocraft.renay.bungeeauth.storage.Storage;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public interface SessionStorage extends Storage {

    Optional<Session> loadSession(UUID uniqueId, String key);

    void saveSession(Session session);

    Queue<Map<String, Session>> loadAllSessions();

    void removeSession(UUID uniqueId, String key);
}
