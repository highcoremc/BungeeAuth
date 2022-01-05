package me.loper.bungeeauth.config;

import com.google.common.collect.ImmutableMap;
import me.loper.bungeeauth.authentication.hash.HashMethodType;
import me.loper.bungeeauth.storage.data.DataStorageType;
import me.loper.bungeeauth.storage.session.SessionStorageType;
import me.loper.configuration.ConfigKey;
import me.loper.storage.nosql.redis.RedisStorageCredentials;
import me.loper.storage.sql.SqlStorageCredentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.loper.configuration.ConfigKeyTypes.key;

public final class ConfigKeys {

    private ConfigKeys() {}

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<SqlStorageCredentials> POSTGRES_CREDENTIALS = key(c -> {
        int maxPoolSize = c.getInteger("data.postgresql.pool-settings.maximum-pool-size", c.getInteger("data.postgresql.pool-size", 10));
        int minIdle = c.getInteger("data.postgresql.pool-settings.minimum-idle", maxPoolSize);
        int maxLifetime = c.getInteger("data.postgresql.pool-settings.maximum-lifetime", 1800000);
        int connectionTimeout = c.getInteger("data.postgresql.pool-settings.connection-timeout", 5000);

        Map<String, String> props = ImmutableMap.copyOf(
            c.getStringMap("data.postgresql.pool-settings.properties", ImmutableMap.of()));

        return new SqlStorageCredentials(
                c.getString("data.postgresql.address", null),
                c.getString("data.postgresql.database", null),
                c.getString("data.postgresql.username", null),
                c.getString("data.postgresql.password", null),
                maxPoolSize, minIdle, maxLifetime, connectionTimeout, props
        );
    });

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<RedisStorageCredentials> REDIS_CREDENTIALS = key(c -> {
        int maxPoolSize = c.getInteger("redis.pool-settings.maximum-pool-size", c.getInteger("redis.pool-size", 10));
        int connectionTimeout = c.getInteger("redis.pool-settings.connection-timeout", 5000);

        return new RedisStorageCredentials(
                c.getString("session.redis.address", null),
                c.getString("session.redis.password", null),
                c.getInteger("session.redis.database", 0),
                maxPoolSize, connectionTimeout
        );
    });

    /**
     * The session settings, username, password, etc for use by any database
     */
    public static final ConfigKey<RedisStorageCredentials> CACHE_REDIS = key(c -> {
        int maxPoolSize = c.getInteger("session.redis.maximum-pool-size", c.getInteger("session.redis.pool-size", 3));
        int connectionTimeout = c.getInteger("session.redis.connection-timeout", 5000);

        return new RedisStorageCredentials(
                c.getString("session.redis.address", null),
                c.getString("session.redis.password", null),
                c.getInteger("session.redis.database", 0),
                connectionTimeout, maxPoolSize
        );
    });

    public static final ConfigKey<HashMethodType> HASH_METHOD_TYPE = key(
            c -> HashMethodType.parse(c.getString("hash-method", "bcrypt")));

    public static final ConfigKey<Integer> SESSION_LIFETIME = key(
            c -> c.getInteger("session.lifetime", 3600));

    public static final ConfigKey<Integer> MAX_AUTH_ATTEMPTS = key(
            c -> c.getInteger("settings.max-auth-attempts", 5));

    public static final ConfigKey<Integer> MAX_AUTH_TIME = key(
        c -> c.getInteger("settings.max-auth-time", 500));;

    public static final ConfigKey<Integer> BAN_TIME_MINUTES = key(
            c -> c.getInteger("settings.ban-time-minutes", 10));

    public static final ConfigKey<Integer> MIN_PASSWORD_LENGTH = key(
            c -> c.getInteger("settings.min-password-length", 4));

    public static final ConfigKey<String> PROTOCOL_REGEX = key(
            c -> c.getString("allowed-protocols", "*"));

    /**
     * The prefix for any SQL tables
     */
    public static final ConfigKey<String> DATA_SQL_TABLE_PREFIX = key(
        c -> c.getString("data.table-prefix", c.getString("data.table_prefix", "bungeeauth_")));

    public static final ConfigKey<String> SESSION_CHANNEL_PREFIX = key(
        c -> c.getString("session.channel", c.getString("session.channel", "bungeeauth")));

    /**
     * The name of the storage method being used for data
     */
    public static final ConfigKey<DataStorageType> DATA_STORAGE_METHOD = key(
        c -> DataStorageType.parse(c.getString("data-storage-method", "postgresql"), DataStorageType.POSTGRESQL));

    /**
     * The name of the storage method being used for session
     */
    public static final ConfigKey<SessionStorageType> CACHE_STORAGE_METHOD = key(
        c -> SessionStorageType.parse(c.getString("session-storage-method", "redis"), SessionStorageType.REDIS));

    public static final ConfigKey<List<String>> GAME_SERVERS = key(
            c -> c.getStringList("servers.game", new ArrayList<>()));

    public static final ConfigKey<List<String>> LOGIN_SERVERS = key(
            c -> c.getStringList("servers.login", new ArrayList<>()));

    public static final ConfigKey<Map<String, List<String>>> WHITELIST_USERS = key(
        c -> c.getListString("white-list", new HashMap<>()));

}
