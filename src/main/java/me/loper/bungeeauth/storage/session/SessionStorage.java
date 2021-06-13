package me.loper.bungeeauth.storage.session;

import me.loper.storage.Storage;

import java.util.*;

public interface SessionStorage extends Storage {

    Optional<Session> loadSession(UUID uniqueId, String key);

    void saveSession(Session session);

    Queue<Map<String, Session>> loadAllSessions();

    void removeSession(UUID uniqueId, String key);

    Map<String, Session> loadSessions(UUID uniqueId);

    void removeSession(List<Session> sessions);
}
