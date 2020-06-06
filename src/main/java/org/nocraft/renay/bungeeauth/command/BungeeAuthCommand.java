package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.plugin.Command;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.util.Registerable;
import org.nocraft.renay.bungeeauth.util.Shutdownable;

public abstract class BungeeAuthCommand extends Command implements Registerable, Shutdownable {

    private final BungeeAuthPlugin plugin;

    public BungeeAuthCommand(BungeeAuthPlugin plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void register() {
        this.plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void unregister() {
        this.plugin.getProxy().getPluginManager().unregisterCommand(this);
    }

    @Override
    public void shutdown() {
        unregister();
    }
}
