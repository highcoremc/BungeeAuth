package org.nocraft.renay.bungeeauth.config;

import com.google.common.collect.ImmutableMap;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodType;
import org.nocraft.renay.bungeeauth.storage.data.DataStorageType;
import org.nocraft.renay.bungeeauth.storage.misc.SessionStorageCredentials;
import org.nocraft.renay.bungeeauth.storage.misc.DatabaseStorageCredentials;
import org.nocraft.renay.bungeeauth.storage.session.SessionStorageType;
import org.nocraft.renay.bungeeauth.util.ImmutableCollectors;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.nocraft.renay.bungeeauth.config.ConfigKeyTypes.customKey;
import static org.nocraft.renay.bungeeauth.config.ConfigKeyTypes.enduringKey;

/**
 * All of the {@link ConfigKey}s used by LuckPerms.
 *
 * <p>The {@link #getKeys()} method and associated behaviour allows this class
 * to function a bit like an enum, but with generics.</p>
 */
public final class ConfigKeys {

    private ConfigKeys() {}

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<DatabaseStorageCredentials> DATABASE_VALUES = enduringKey(customKey(c -> {
        int maxPoolSize = c.getInteger("data.pool-settings.maximum-pool-size", c.getInteger("data.pool-size", 10));
        int minIdle = c.getInteger("data.pool-settings.minimum-idle", maxPoolSize);
        int maxLifetime = c.getInteger("data.pool-settings.maximum-lifetime", 1800000);
        int connectionTimeout = c.getInteger("data.pool-settings.connection-timeout", 5000);
        Map<String, String> props = ImmutableMap.copyOf(c.getStringMap("data.pool-settings.properties", ImmutableMap.of()));

        return new DatabaseStorageCredentials(
                c.getString("data.address", null),
                c.getString("data.database", null),
                c.getString("data.username", null),
                c.getString("data.password", null),
                maxPoolSize, minIdle, maxLifetime, connectionTimeout, props
        );
    }));

    /**
     * The session settings, username, password, etc for use by any database
     */
    public static final ConfigKey<SessionStorageCredentials> CACHE_VALUES = enduringKey(customKey(c -> {
        int maxPoolSize = c.getInteger("session.maximum-pool-size", c.getInteger("session.pool-size", 3));
        int connectionTimeout = c.getInteger("session.connection-timeout", 5000);

        return new SessionStorageCredentials(
                c.getString("session.address", null),
                c.getString("session.database", null),
                c.getString("session.username", null),
                c.getString("session.password", null),
                connectionTimeout, maxPoolSize
        );
    }));

    public static final ConfigKey<HashMethodType> HASH_METHOD_TYPE = enduringKey(customKey(
            c -> HashMethodType.parse(c.getString("hash-method", "bcrypt"))));

    public static final ConfigKey<Integer> SESSION_TIMEOUT = enduringKey(customKey(
            c -> c.getInteger("session.timeout", 3600)));

    public static final ConfigKey<Integer> MAX_AUTH_ATTEMPTS = enduringKey(customKey(
            c -> c.getInteger("settings.max-auth-attempts", 5)));

    public static final ConfigKey<Integer> BAN_TIME_MINUTES = enduringKey(customKey(
            c -> c.getInteger("settings.ban-time-minutes", 10)));

    public static final ConfigKey<Integer> MIN_PASSWORD_LENGTH = enduringKey(customKey(
            c -> c.getInteger("settings.min-password-length", 4)));

    public static final ConfigKey<String> PROTOCOL_REGEX = enduringKey(customKey(
            c -> c.getString("allowed-protocols", "*")));

    /**
     * The prefix for any SQL tables
     */
    public static final ConfigKey<String> DATA_SQL_TABLE_PREFIX = enduringKey(customKey(c -> c.getString("data.table-prefix", c.getString("data.table_prefix", "bungeeauth_"))));

    public static final ConfigKey<String> SESSION_CHANNEL_PREFIX = enduringKey(customKey(c -> c.getString("session.channel", c.getString("session.channel", "bungeeauth"))));

    /**
     * The name of the storage method being used for data
     */
    public static final ConfigKey<DataStorageType> DATA_STORAGE_METHOD = enduringKey(customKey(c -> DataStorageType.parse(c.getString("data-storage-method", "postgresql"), DataStorageType.POSTGRESQL)));

    /**
     * The name of the storage method being used for session
     */
    public static final ConfigKey<SessionStorageType> CACHE_STORAGE_METHOD = enduringKey(customKey(c -> SessionStorageType.parse(c.getString("session-storage-method", "redis"), SessionStorageType.REDIS)));

    public static final ConfigKey<List<String>> GAME_SERVERS = enduringKey(customKey(
            c -> c.getStringList("servers.game", new ArrayList<>())));

    public static final ConfigKey<List<String>> LOGIN_SERVERS = enduringKey(customKey(
            c -> c.getStringList("servers.login", new ArrayList<>())));


    private static final List<ConfigKeyTypes.BaseConfigKey<?>> KEYS;

    static {
        // get a list of all keys
        KEYS = Arrays.stream(ConfigKeys.class.getFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> ConfigKey.class.equals(f.getType()))
                .map(f -> {
                    try {
                        return (ConfigKeyTypes.BaseConfigKey<?>) f.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(ImmutableCollectors.toList());

        // set ordinal values
        for (int i = 0; i < KEYS.size(); i++) {
            KEYS.get(i).ordinal = i;
        }
    }

    /**
     * Gets a list of the keys defined in this class.
     *
     * @return the defined keys
     */
    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }

}
