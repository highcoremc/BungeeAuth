package ru.yooxa.bungee.auth;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class IpProtect implements Listener
{
    public IpProtect(AuthManager manager)
    {
        this.ips = new HashMap();

        load(manager.getConfig());
        ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
    }

    Map<String, String> ips;

    public void load(Configuration config)
    {
        for (String line : config.getStringList("IpProtect")) {
            String[] s = line.split(";");
            this.ips.put(s[0].toLowerCase(), s[1]);
        }
    }

    @EventHandler(priority = -64)
    public void onLogin(PreLoginEvent e)
    {
        String name = e.getConnection().getName().toLowerCase();
        String ip = e.getConnection().getAddress().getAddress().getHostAddress();
        if (this.ips.containsKey(name) && !((String) this.ips.get(name)).equals(ip)) {
            e.setCancelled(true);
            e.setCancelReason("§cДанный аккаунт вам не принадлежит");
        }
    }
}


