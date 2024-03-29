package me.loper.bungeeauth.storage.session;

import com.google.common.collect.ImmutableSet;
import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.storage.nosql.redis.RedisConnectionFactory;

import java.util.Set;

public class SessionStorageFactory {

    private final BungeeAuthPlugin plugin;

    public SessionStorageFactory(BungeeAuthPlugin plugin) {
        this.plugin = plugin;
    }

    public Set<SessionStorageType> getRequiredTypes() {
        return ImmutableSet.of(this.plugin.getConfiguration().get(ConfigKeys.CACHE_STORAGE_METHOD));
    }

    public SimpleSessionStorage getInstance() {
        SessionStorageType type = this.plugin.getConfiguration().get(ConfigKeys.CACHE_STORAGE_METHOD);
        this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");
        SimpleSessionStorage storage = new SimpleSessionStorage(this.plugin, createNewImplementation(type));

        storage.init();
        return storage;
    }

    private SessionStorage createNewImplementation(SessionStorageType method) {
        switch (method) {
            case REDIS:
                return new SessionRedisStorage(
                        new RedisConnectionFactory<>(this.plugin.getConfiguration().get(ConfigKeys.CACHE_REDIS)),
                        this.plugin.getConfiguration().get(ConfigKeys.SESSION_CHANNEL_PREFIX)
                );
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }
}
