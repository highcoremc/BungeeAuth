package org.nocraft.renay.bungee.auth.storage;

import com.google.common.collect.ImmutableSet;
import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.config.ConfigKeys;
import org.nocraft.renay.bungee.auth.storage.implementation.StorageImplementation;
import org.nocraft.renay.bungee.auth.storage.implementation.custom.CustomStorageProviders;
import org.nocraft.renay.bungee.auth.storage.implementation.sql.SqlStorage;
import org.nocraft.renay.bungee.auth.storage.implementation.sql.connection.hikari.PostgreConnectionFactory;

import java.util.Set;

public class StorageFactory {
    private final BungeeAuth plugin;

    public StorageFactory(BungeeAuth plugin) {
        this.plugin = plugin;
    }

    public Set<StorageType> getRequiredTypes() {
        return ImmutableSet.of(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD));
    }

    public Storage getInstance() {
        StorageType type = this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
        this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");
        Storage storage = new Storage(this.plugin, createNewImplementation(type));

        storage.init();
        return storage;
    }

    private StorageImplementation createNewImplementation(StorageType method) {
        switch (method) {
            case CUSTOM:
                return CustomStorageProviders.getProvider().provide(this.plugin);
            case POSTGRESQL:
                return new SqlStorage(
                        this.plugin,
                        new PostgreConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }
}
