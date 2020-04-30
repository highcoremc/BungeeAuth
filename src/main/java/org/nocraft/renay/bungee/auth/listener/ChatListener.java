package org.nocraft.renay.bungee.auth.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungee.auth.BungeeAuth;

public class ChatListener implements Listener {

    private final BungeeAuth plugin;

    public ChatListener(BungeeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
    }
}