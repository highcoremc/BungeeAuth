package org.nocraft.renay.bungeeauth.storage.session;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.implementation.nosql.RedisConnectionFactory;

import java.util.Optional;
import java.util.UUID;

public class SessionRedisStorage implements SessionStorage {
    private final RedisConnectionFactory<Session> connectionFactory;
    private final BungeeAuthPlugin plugin;
    private final String channel;

    public SessionRedisStorage(BungeeAuthPlugin plugin, RedisConnectionFactory<Session> factory, String channel) {
        this.connectionFactory = factory;
        this.channel = channel;
        this.plugin = plugin;
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() {
        this.connectionFactory.init();
    }

    @Override
    public void shutdown() {
        this.connectionFactory.shutdown();
    }

    @Override
    public Optional<Session> loadSession(UUID uniqueId) {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            Session result = conn.sync().get(uniqueId.toString());

            return null == result ? Optional.empty() : Optional.of(result);
        }
    }

    @Override
    public void saveSession(Session session) {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            conn.sync().set(session.userId.toString(), session);
        }
    }
}
