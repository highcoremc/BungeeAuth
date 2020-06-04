package org.nocraft.renay.bungeeauth.storage.data;

import com.google.common.collect.ImmutableSet;
import org.nocraft.renay.bungeeauth.BungeeAuth;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
import org.nocraft.renay.bungeeauth.storage.data.implementation.DataSqlStorage;
import org.nocraft.renay.bungeeauth.storage.implementation.custom.CustomStorageProviders;
import org.nocraft.renay.bungeeauth.storage.implementation.sql.connection.hikari.PostgreConnectionFactory;

import java.util.Set;

public class DataStorageFactory {
    private final BungeeAuth plugin;

    public DataStorageFactory(BungeeAuth plugin) {
        this.plugin = plugin;
    }

    public Set<DataStorageType> getRequiredTypes() {
        return ImmutableSet.of(this.plugin.getConfiguration().get(ConfigKeys.DATA_STORAGE_METHOD));
    }

    public SimpleDataStorage getInstance() {
        DataStorageType type = this.plugin.getConfiguration().get(ConfigKeys.DATA_STORAGE_METHOD);
        this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");
        SimpleDataStorage storage = new SimpleDataStorage(this.plugin, createNewImplementation(type));

        storage.init();
        return storage;
    }

    private DataStorage createNewImplementation(DataStorageType method) {
        switch (method) {
            case CUSTOM:
                return CustomStorageProviders.getProvider().provide(this.plugin);
            case POSTGRESQL:
                return new DataSqlStorage(
                        this.plugin,
                        new PostgreConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.DATA_SQL_TABLE_PREFIX)
                );
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }
}
