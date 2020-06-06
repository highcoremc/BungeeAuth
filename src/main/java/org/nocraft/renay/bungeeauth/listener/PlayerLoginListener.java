package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.event.FailedCreateSessionEvent;
import org.nocraft.renay.bungeeauth.event.LoginFailedEvent;
import org.nocraft.renay.bungeeauth.event.LoginSuccessfulEvent;
import org.nocraft.renay.bungeeauth.event.RegisterSuccessfulEvent;

import java.util.Optional;
import java.util.UUID;

public class PlayerLoginListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;

    public PlayerLoginListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSuccessfulPlayerLogin(LoginSuccessfulEvent e) {
        Optional<ProxiedPlayer> p = plugin.getPlayer(e.getPlayerId());

        if (!p.isPresent()) {
            return;
        }

        ProxiedPlayer player = p.get();

         // TODO: connect player to lobby, or previous played server
        player.connect(plugin.getProxy().getServerInfo("earth"));

        plugin.getLogger().info(String.format("Player %s was a successfully authenticated", player.getName()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSuccessfulPlayerRegister(RegisterSuccessfulEvent e) {
        UUID uniqueId = e.getPlayerId();

        Optional<ProxiedPlayer> p = this.plugin.getPlayer(uniqueId);

        if (!p.isPresent()) {
            return;
        }

        plugin.getLogger().info(String.format("Player %s was a successfully registered", p.get().getName()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailCreteSession(FailedCreateSessionEvent e) {
        UUID uniqueId = e.getPlayerId();

        plugin.getLogger().info("Failed create session for player " + uniqueId);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailedLogin(LoginFailedEvent e) {
        UUID uniqueId = e.getPlayerId();
        Optional<ProxiedPlayer> p = this.plugin
                .getPlayer(uniqueId);

        if (!p.isPresent()) {
            plugin.getLogger().info("Failed authenticate player " + uniqueId + " and he was leave before kick.");
            return;
        }

        String message = ChatColor.translateAlternateColorCodes('&',
                "&c&lFAIL AUTHENTICATION\n"+
                "&fWe are can not process you login,\n"+
                "&fplease contact with administration.");
        p.get().disconnect(TextComponent.fromLegacyText(message));
    }
}
