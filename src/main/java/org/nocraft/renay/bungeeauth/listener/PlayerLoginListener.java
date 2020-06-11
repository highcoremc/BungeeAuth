package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.event.*;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.util.TitleBarApi;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerLoginListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;

    public PlayerLoginListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.plugin.getScheduler().asyncRepeating(
                this::cleanupSessions, 10, TimeUnit.MINUTES);
    }

    private void cleanupSessions() {
        Queue<Map<String, Session>> playerSessions = this.plugin
                .getSessionStorage()
                .loadAll().join();
        for (Map<String, Session> sessions : playerSessions) {
            sessions.forEach((key, session) -> {
                Date endTime = session.time.endTime;
                if (System.currentTimeMillis() > endTime.getTime()) {
                    this.plugin.getSessionStorage().remove(
                            session.userId, key);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSuccessfulPlayerLogin(PlayerSuccessfulLoginEvent e) {
        plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            // TODO: connect player to lobby, or previous played server
            player.connect(plugin.getProxy().getServerInfo("earth"));

            // remove old title
            TitleBarApi.send(player, "", "", 0, 10, 0);

            // clean chat after login
            for (int i = 0; i < 23; i++) {
                player.sendMessage(new TextComponent());
            }

            String msg = ChatColor.GREEN + "Successfully authenticated!";
            player.sendMessage(TextComponent.fromLegacyText(msg));
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSuccessfulPlayerRegister(PlayerRegisteredEvent e) {
        UUID uniqueId = e.getPlayerId();
        Optional<ProxiedPlayer> p = this.plugin
                .getPlayer(uniqueId);

        if (p.isPresent()) {
            String message = "Player %s was a successfully registered!";
            plugin.getLogger().info(String.format(message, p.get().getName()));
            this.plugin.getAttemptManager().clearAttempts(uniqueId);
        }

        BungeeAuthPlayer player = this.plugin.getAuthPlayers()
                .get(uniqueId);
        this.plugin.getDataStorage().saveUser(player.user);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailCreteSession(FailedCreationSessionEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            String message = ChatColor.translateAlternateColorCodes('&',
                            "&c&lFAIL AUTHENTICATION\n" +
                            "&fServer can not handle your request,\n" +
                            "&fPlease contact with administrator...");
            player.disconnect(TextComponent.fromLegacyText(message));
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailedLogin(PlayerLoginFailed e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            String message = ChatColor.translateAlternateColorCodes('&',
                            "&c&lFAIL AUTHENTICATION\n" +
                            "&fWe are can not process you login,\n" +
                            "&fplease contact with administration.");
            player.disconnect(TextComponent.fromLegacyText(message));
        });
    }
}
