package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.plugin.Listener;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.util.Registerable;
import org.nocraft.renay.bungeeauth.util.Shutdownable;

public abstract class BungeeAuthListener implements Registerable, Shutdownable, Listener {

    private final BungeeAuthPlugin plugin;

    public BungeeAuthListener(BungeeAuthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        this.plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @Override
    public void unregister() {
        this.plugin.getProxy().getPluginManager().unregisterListener(this);
    }

    @Override
    public void shutdown() {
        unregister();
    }
}
