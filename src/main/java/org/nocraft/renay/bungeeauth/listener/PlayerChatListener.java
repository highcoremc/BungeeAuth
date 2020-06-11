package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerChatListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;

    public PlayerChatListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if (!plugin.isAuthenticated(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}