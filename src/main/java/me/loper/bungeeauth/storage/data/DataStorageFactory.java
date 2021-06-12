package me.loper.bungeeauth.storage.data;

import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.storage.data.implementation.DataRedisStorage;
import me.loper.bungeeauth.storage.data.implementation.DataSqlStorage;
import me.loper.bungeeauth.storage.implementation.nosql.RedisConnectionFactory;
import me.loper.bungeeauth.storage.implementation.sql.connection.hikari.PostgreConnectionFactory;
import me.loper.bungeeauth.storage.misc.DatabaseStorageCredentials;
import me.loper.bungeeauth.BungeeAuthPlugin;

public class DataStorageFactory {
    private final BungeeAuthPlugin plugin;

    public DataStorageFactory(BungeeAuthPlugin plugin) {
        this.plugin = plugin;
    }

    public SimpleDataStorage getInstance() {
        DataStorageType type = this.plugin.getConfiguration().get(ConfigKeys.DATA_STORAGE_METHOD);
        this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");
        SimpleDataStorage storage = new SimpleDataStorage(this.plugin, createNewImplementation(type));

        storage.init();
        return storage;
    }

    private DataStorage createNewImplementation(DataStorageType method) {
        DatabaseStorageCredentials credentials = this.plugin.getConfiguration()
            .get(ConfigKeys.DATABASE_VALUES);

        switch (method) {
            case POSTGRESQL:
                return new DataSqlStorage(this.plugin, new PostgreConnectionFactory(credentials),
                        this.plugin.getConfiguration().get(ConfigKeys.DATA_SQL_TABLE_PREFIX)
                );
            case REDIS:
                return new DataRedisStorage(new RedisConnectionFactory<>(credentials));
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }
}
