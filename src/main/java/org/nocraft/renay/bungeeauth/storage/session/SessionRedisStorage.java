package org.nocraft.renay.bungeeauth.storage.session;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.implementation.nosql.RedisConnectionFactory;

import java.util.Optional;
import java.util.UUID;

public class SessionRedisStorage implements SessionStorage {

    private final RedisConnectionFactory<Session> connectionFactory;
    private final BungeeAuthPlugin plugin;

    private final String SESSION_KEY;

    public SessionRedisStorage(BungeeAuthPlugin plugin, RedisConnectionFactory<Session> factory, String channel) {
        this.connectionFactory = factory;
        this.plugin = plugin;

        SESSION_KEY = channel + ":session";
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() {
        this.connectionFactory.init();
        this.connectionFactory.getConnection();
    }

    @Override
    public void shutdown() {
        this.connectionFactory.shutdown();
    }

    @Override
    public Optional<Session> loadSession(UUID uniqueId, String key) {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            Session result = conn.sync().hget(craftKey(uniqueId), key);
            return null == result ? Optional.empty() : Optional.of(result);
        }
    }

    @Override
    public void saveSession(Session session) {
        session.getIOLock().lock();
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            conn.sync().hset(craftKey(session.userId), session.ipAddress, session);
        } finally {
            session.getIOLock().unlock();
        }
    }

    public String craftKey(UUID id) {
        return SESSION_KEY + ':' + id.toString();
    }
}
