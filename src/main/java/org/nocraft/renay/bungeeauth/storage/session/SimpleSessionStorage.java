package org.nocraft.renay.bungeeauth.storage.session;

import org.nocraft.renay.bungeeauth.BungeeAuth;

public class SimpleSessionStorage {

    private final SessionStorage implementation;
    private final BungeeAuth plugin;

    public SimpleSessionStorage(BungeeAuth plugin, SessionStorage implementation) {
        this.implementation = implementation;
        this.plugin = plugin;
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation");
            e.printStackTrace();
        }
    }
}
