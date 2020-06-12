package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.ServerType;

public class PlayerChatListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;

    public PlayerChatListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        plugin.getLogger().info(e.getMessage());
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String message = e.getMessage();

        if (!isLoginCommand(message) && !plugin.isAuthenticated(player.getUniqueId())) {
            e.setCancelled(true);
        }

//        ServerInfo info = player.getServer().getInfo();
//        ServerType type = plugin.getServerManager()
//                .getServerType(info);
//        if (type.equals(ServerType.LOGIN)) {
//            e.setCancelled(true);
//        }
    }

    private boolean isLoginCommand(String message) {
        return message.startsWith("/l") ||
                message.startsWith("/reg") ||
                message.startsWith("/login") ||
                message.startsWith("/register");
    }
}