package org.nocraft.renay.bungee.auth.storage.implementation.custom;

import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.storage.implementation.StorageImplementation;

/**
 * A storage provider
 */
@FunctionalInterface
public interface CustomStorageProvider {

    StorageImplementation provide(BungeeAuth plugin);

}
