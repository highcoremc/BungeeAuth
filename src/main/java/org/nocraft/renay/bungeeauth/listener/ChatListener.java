package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;

public class ChatListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;

    public ChatListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }
    }
}