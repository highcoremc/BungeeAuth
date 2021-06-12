package me.loper.bungeeauth.storage.data.implementation;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.UserPassword;
import me.loper.bungeeauth.storage.implementation.nosql.RedisConnectionFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import me.loper.bungeeauth.storage.data.DataStorage;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DataRedisStorage implements DataStorage {

    private final static String STORAGE_KEY = "datasync";

    private final RedisConnectionFactory<User> connectionFactory;

    public DataRedisStorage(RedisConnectionFactory<User> factory) {
        this.connectionFactory = factory;
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() {
        this.connectionFactory.init();
        try (StatefulRedisConnection<String, User> conn = this.connectionFactory.getConnection()) {
            String result = conn.sync().ping();
            if (!result.equals("PONG")) {
                throw new IllegalStateException("Can not get connection.");
            }
        }
    }

    @Override
    public Optional<User> loadUser(UUID uniqueId) {
        try (StatefulRedisConnection<String, User> conn = this.connectionFactory.getConnection()) {
            User result = conn.sync().hget(STORAGE_KEY, uniqueId.toString());

            if (result == null) {
                return Optional.empty();
            }

            return Optional.of(result);
        }
    }

    @Override
    public Optional<User> loadUser(String playerName) throws Exception {
        return Optional.empty();
    }

    @Override
    public void saveUser(User user) {
        try (StatefulRedisConnection<String, User> conn = this.connectionFactory.getConnection()) {
            conn.sync().hset(STORAGE_KEY, user.uniqueId.toString(), user);
        }
    }

    @Override
    public void changeUserPassword(UserPassword password) {
    }

    @Override
    public Set<UUID> getUniqueUsers() {
        // TODO: implement or delete this method
        return null;
    }

    @Override
    public @Nullable UUID getPlayerUniqueId(String username) {
        // TODO: implement or delete this method
        return null;
    }

    @Override
    public @Nullable String getPlayerName(UUID uniqueId) {
        // TODO: implement or delete this method
        return null;
    }

    @Override
    public void shutdown() {
        this.connectionFactory.shutdown();
    }
}
