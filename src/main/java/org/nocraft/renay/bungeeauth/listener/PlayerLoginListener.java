package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.PlayerWrapper;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.event.FailedCreationSessionEvent;
import org.nocraft.renay.bungeeauth.event.PlayerAuthenticatedEvent;
import org.nocraft.renay.bungeeauth.event.PlayerLoginFailedEvent;
import org.nocraft.renay.bungeeauth.event.PlayerRegisteredEvent;
import org.nocraft.renay.bungeeauth.server.ServerManager;
import org.nocraft.renay.bungeeauth.server.ServerType;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.util.TitleBarApi;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerLoginListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;
    private final ServerManager connector;

    public PlayerLoginListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.connector = plugin.getServerManager();
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
    public void onSuccessfulEventChangeStatus(PlayerAuthenticatedEvent e) {
        BungeeAuthPlayer player = this.plugin.getAuthPlayers()
                .get(e.getPlayerId());
        if (player != null) {
            player.authenticated();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSuccessfulPlayerLogin(PlayerAuthenticatedEvent e) {
        plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            // clean chat after login
            for (int i = 0; i < 23; i++) {
                player.sendMessage(new TextComponent());
            }

            // remove old title
            TitleBarApi.send(player, new Message(""), new Message(""), 0, 15, 0);
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.USER_AUTHENTICATED);
            player.sendMessage(message.asComponent());

            // log message what player is authenticated
            logAuthenticatedPlayer(player);

            // needs to connect player to game servers?
            if (!e.isConnectHandled()) {
                connector.connect(ServerType.GAME, player);
            }
        });
    }

    private void logAuthenticatedPlayer(ProxiedPlayer p) {
        plugin.getLogger().info(String.format("Player %s successfully login as Offline player", p.getName()));
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
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.FAIL_SESSION_CREATION);
            player.disconnect(message.asComponent());

            String msg = "Player %s failed create session with ip %s.";
            plugin.getLogger().warning(String.format(msg, player.getName(), PlayerWrapper.wrap(player).getIpAddress()));
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailedLogin(PlayerLoginFailedEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.FAILED_AUTHENTICATION);
            player.disconnect(message.asComponent());

            String msg = "Player %s failed register with ip %s.";
            plugin.getLogger().warning(String.format(msg, player.getName(), PlayerWrapper.wrap(player).getIpAddress()));
        });
    }
}
