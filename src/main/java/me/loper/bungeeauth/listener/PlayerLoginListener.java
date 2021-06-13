package me.loper.bungeeauth.listener;

import me.loper.bungeeauth.server.ServerType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import me.loper.bungeeauth.BungeeAuthPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.PlayerWrapper;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.event.FailedCreationSessionEvent;
import me.loper.bungeeauth.event.PlayerAuthenticatedEvent;
import me.loper.bungeeauth.event.PlayerLoginFailedEvent;
import me.loper.bungeeauth.server.ServerManager;
import me.loper.bungeeauth.storage.session.Session;
import me.loper.bungeeauth.util.TitleBarApi;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerLoginListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;
    private final ServerManager connector;

    public PlayerLoginListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.connector = plugin.getServerManager();
        this.plugin.getScheduler().asyncRepeating(this::cleanupSessions, 10, TimeUnit.MINUTES);
    }

    private void cleanupSessions() {
        Queue<Map<String, Session>> playerSessions = this.plugin
                .getSessionStorage().loadAll().join();

        for (Map<String, Session> sessions : playerSessions) {
            sessions.forEach(this::dropSession);
        }
    }

    private void dropSession(String key, Session session) {
        Date endTime = session.lifeTime.endTime;
        if (System.currentTimeMillis() > endTime.getTime()) {
            this.plugin.getSessionStorage().remove(session.userId, key);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSuccessfulEventChangeStatus(PlayerAuthenticatedEvent e) {
        BungeeAuthPlayer player = this.plugin.getAuthPlayer(e.getPlayerId());

        if (player != null) {
            player.authenticated();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSuccessfulPlayerLogin(PlayerAuthenticatedEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            cleanChat(player);

            // remove old title
            TitleBarApi.send(player, new Message(""), new Message(""), 0, 15, 0);
            Message message = plugin.getMessageConfig().get(MessageKeys.USER_AUTHENTICATED);
            player.sendMessage(message.asComponent());

            // log message what player is authenticated
            logAuthenticatedPlayer(player);

            // needs to connect player to game servers?
            if (!e.isConnectHandled()) {
                this.connector.connect(ServerType.GAME, player);
            }
        });
    }

    private void cleanChat(ProxiedPlayer player) {
        // clean chat after login
        for (int i = 0; i < 23; i++) {
            player.sendMessage(new TextComponent());
        }
    }

    private void logAuthenticatedPlayer(ProxiedPlayer p) {
        this.plugin.getLogger().info(String.format("Player %s successfully login as Offline player", p.getName()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailCreteSession(FailedCreationSessionEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.FAIL_SESSION_CREATION);
            player.disconnect(message.asComponent());

            String msg = "Player %s failed create session with ip %s.";
            this.plugin.getLogger().warning(String.format(msg, player.getName(), PlayerWrapper.wrap(player).getIpAddress()));
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFailedLogin(PlayerLoginFailedEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.FAILED_AUTHENTICATION);
            player.disconnect(message.asComponent());

            String msg = "Player %s failed register with ip %s.";
            this.plugin.getLogger().warning(String.format(msg, player.getName(), PlayerWrapper.wrap(player).getIpAddress()));
        });
    }
}
