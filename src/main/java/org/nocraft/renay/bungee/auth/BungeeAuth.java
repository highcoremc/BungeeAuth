package org.nocraft.renay.bungee.auth;

import net.md_5.bungee.api.plugin.Plugin;
import org.nocraft.renay.bungee.auth.Listener.ChatListener;
import org.nocraft.renay.bungee.auth.Listener.LoginListener;

public class BungeeAuth extends Plugin {

    public void onEnable() {

        getProxy().getPluginManager().registerListener(this, new ChatListener(this));
        getProxy().getPluginManager().registerListener(this, new LoginListener(this));
    }
}
