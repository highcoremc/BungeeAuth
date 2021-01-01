package org.nocraft.renay.bungeeauth.storage.data;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
import org.nocraft.renay.bungeeauth.storage.data.implementation.DataRedisStorage;
import org.nocraft.renay.bungeeauth.storage.data.implementation.DataSqlStorage;
import org.nocraft.renay.bungeeauth.storage.implementation.nosql.RedisConnectionFactory;
import org.nocraft.renay.bungeeauth.storage.implementation.sql.connection.hikari.PostgreConnectionFactory;
import org.nocraft.renay.bungeeauth.storage.misc.DatabaseStorageCredentials;

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
