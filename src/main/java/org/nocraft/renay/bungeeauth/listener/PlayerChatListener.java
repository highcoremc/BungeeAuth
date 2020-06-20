package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;

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
        if (plugin.isAuthenticated(player)) {
            return;
        }

        String message = e.getMessage();

        plugin.getLogger().info("Player " + player.getName() + " tried to say: " + message);

        if (!isLoginCommand(message)) {
            plugin.getLogger().info(String.format(
                    "Player %s say %s and we was cancelled.",
                    player.getName(), message
            ));
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteResponseEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer)
                event.getSender();
        if (!plugin.isAuthenticated(player)) {
            event.getSuggestions().clear();
        }
    }

    private boolean isLoginCommand(String message) {
        String[] splitted = message.split(" ");
        String cmd = splitted[0];

        return cmd.equals("/l") ||
                cmd.equals("/reg") ||
                cmd.equals("/login") ||
                cmd.equals("/register");
    }
}