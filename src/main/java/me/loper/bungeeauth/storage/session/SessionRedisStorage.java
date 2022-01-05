package me.loper.bungeeauth.storage.session;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.storage.nosql.redis.RedisConnectionFactory;

import java.util.*;

public class SessionRedisStorage implements SessionStorage {

    private final RedisConnectionFactory<Session> connectionFactory;

    private final String SESSION_KEY;

    public SessionRedisStorage(RedisConnectionFactory<Session> factory, String channel) {
        this.connectionFactory = factory;

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
            String key = craftKey(session.userId);

            Date expires = session.lifeTime.endTime;
            if (null != session.lifeTime.closedTime) {
                expires = session.lifeTime.closedTime;
            }

            conn.sync().hset(key, session.ipAddress, session);
            conn.sync().expireat(key, expires);
        } finally {
            session.getIOLock().unlock();
        }
    }

    @Override
    public Queue<Map<String, Session>> loadAllSessions() {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            List<String> keys = conn.sync().keys(SESSION_KEY + ":*");
            Queue<Map<String, Session>> result = new LinkedList<>();
            for (String key : keys) {
                result.add(conn.sync().hgetall(key));
            }
            return result;
        }
    }

    @Override
    public void removeSession(UUID uniqueId, String key) {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            conn.sync().hdel(craftKey(uniqueId), key);
        }
    }

    @Override
    public Map<String, Session> loadSessions(UUID uniqueId) {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            return conn.sync().hgetall(craftKey(uniqueId));
        }
    }

    @Override
    public void removeSession(List<Session> sessions) {
        try (StatefulRedisConnection<String, Session> conn = this.connectionFactory.getConnection()) {
            for (Session session : sessions) {
                conn.sync().hdel(craftKey(session.userId), session.ipAddress);
            }
        }
    }

    public String craftKey(UUID id) {
        return SESSION_KEY + ':' + id.toString();
    }
}
