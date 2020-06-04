package org.nocraft.renay.bungeeauth.storage.data.implementation;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.nocraft.renay.bungeeauth.BungeeAuth;
import org.nocraft.renay.bungeeauth.storage.data.DataStorage;
import org.nocraft.renay.bungeeauth.storage.implementation.nosql.RedisConnectionFactory;
import org.nocraft.renay.bungeeauth.user.User;

import java.util.Set;
import java.util.UUID;

public class DataNoSqlStorage implements DataStorage {

    private final static String STORAGE_KEY = "datasync";

    private final RedisConnectionFactory connectionFactory;

    public DataNoSqlStorage(BungeeAuth plugin, RedisConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() {
        this.connectionFactory.init();
        try (StatefulRedisConnection<String, byte[]> conn = this.connectionFactory.getConnection()) {
            String result = conn.sync().ping();
            if (!result.equals("PONG")) {
                throw new IllegalStateException("Can not get connection.");
            }
        }
    }

    @Override
    public User loadUser(UUID uniqueId) {
        try (StatefulRedisConnection<String, byte[]> conn = this.connectionFactory.getConnection()) {
            byte[] result = conn.sync().hget(STORAGE_KEY, uniqueId.toString());

            // get user from bytes result

            if (result == null) {
                return null;
            }

            return null;// result;
        }
    }

    @Override
    public void saveUser(User player) {
        try (StatefulRedisConnection<String, byte[]> conn = this.connectionFactory.getConnection()) {
//            conn.sync().hset(STORAGE_KEY, player.getPlayerId().toString(), player.getData());
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() throws Exception {
        // TODO: implement or delete this method
        return null;
    }

    @Override
    public @Nullable UUID getPlayerUniqueId(String username) throws Exception {
        // TODO: implement or delete this method
        return null;
    }

    @Override
    public @Nullable String getPlayerName(UUID uniqueId) throws Exception {
        // TODO: implement or delete this method
        return null;
    }

    @Override
    public void shutdown() {
        this.connectionFactory.shutdown();
    }
}
