package org.nocraft.renay.bungeeauth.storage.session;

import org.nocraft.renay.bungeeauth.BungeeAuth;
import org.nocraft.renay.bungeeauth.storage.ConnectionFactory;

public class SessionNosqlStorage<T> implements SessionStorage {
    private final ConnectionFactory<T> connectionFactory;
    private final BungeeAuth plugin;
    private final String channel;

    public SessionNosqlStorage(BungeeAuth plugin, ConnectionFactory<T> connectionFactory, String channel) {
        this.connectionFactory = connectionFactory;
        this.channel = channel;
        this.plugin = plugin;
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() {
        this.connectionFactory.init();
    }

    @Override
    public void shutdown() {
        this.connectionFactory.shutdown();
    }
}
