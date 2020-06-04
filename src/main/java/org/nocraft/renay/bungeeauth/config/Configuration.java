package org.nocraft.renay.bungeeauth.config;

import org.nocraft.renay.bungeeauth.BungeeAuth;

/**
 * The master configuration used by LuckPerms.
 */
public interface Configuration {

    /**
     * Gets the main plugin instance.
     *
     * @return the plugin instance
     */
    BungeeAuth getPlugin();

    /**
     * Reloads the configuration.
     */
    void reload();

    /**
     * Loads all configuration values.
     */
    void load();

    /**
     * Gets the value of a given context key.
     *
     * @param key the key
     * @param <T> the key return type
     * @return the value mapped to the given key. May be null.
     */
    <T> T get(ConfigKey<T> key);

}
