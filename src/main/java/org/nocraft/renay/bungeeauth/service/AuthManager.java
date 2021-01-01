package org.nocraft.renay.bungeeauth.service;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodType;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.exception.AuthenticationException;
import org.nocraft.renay.bungeeauth.server.ServerManager;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;
import org.nocraft.renay.bungeeauth.storage.entity.SimpleSessionStorage;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.entity.UserPassword;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.util.ImmutableCollectors;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AuthManager {

    private final SimpleSessionStorage sessionStorage;
    private final SimpleDataStorage dataStorage;
    private final BungeeAuthPlugin plugin;

    public AuthManager(BungeeAuthPlugin plugin) {
        this.sessionStorage = plugin.getSessionStorage();
        this.dataStorage = plugin.getDataStorage();
        this.plugin = plugin;
    }

    public void authenticate(PendingConnection conn) {
        try {
            UUID uniqueId = conn.getUniqueId();
            User user = loadUser(conn, uniqueId);

            String host = conn.getAddress().getHostString();
            BungeeAuthPlayer player = new BungeeAuthPlayer(user);

            Optional<Session> session = this.sessionStorage
                .loadSession(uniqueId, host).join();
            if (!player.user.isRegistered()) {
                session.ifPresent(s -> this.sessionStorage.remove(uniqueId, s.ipAddress));
            } else {
                session.ifPresent(player::changeActiveSession);
            }

            this.plugin.addAuthPlayer(player);
        } catch (Exception ex) {
            throw new AuthenticationException(conn.getName(), conn.getUniqueId(), ex);
        }
    }

    private User loadUser(PendingConnection c, UUID uniqueId) {
        Optional<User> userQuery = this.dataStorage
            .loadUser(uniqueId).join();

        if (userQuery.isPresent()) {
            return userQuery.get();
        }

        InetSocketAddress address = (InetSocketAddress) c.getSocketAddress();

        String host = address.getHostString();
        String userName = c.getName();

        return new User(uniqueId, userName, host);
    }

    public void authenticateOnlinePlayers() {
        for (ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
            try {
                authenticate(player.getPendingConnection());
            } catch (AuthenticationException ex) {
                Message message = this.plugin.getMessageConfig()
                    .get(MessageKeys.FAILED_AUTHENTICATION);
                player.disconnect(message.asComponent());
            }
        }
    }

    public void clearSessions(UUID uniqueId) {
        CompletableFuture<Map<String, Session>> sessionsFuture = this.plugin
            .getSessionStorage().loadSessions(uniqueId);

        BungeeAuthPlayer authPlayer = this.plugin.getAuthPlayer(uniqueId);
        Session activeSession = null == authPlayer ? null : authPlayer.session;

        sessionsFuture.thenAccept(sessions -> {
            Stream<Session> result = sessions.values().stream();

            if (null != activeSession) {
                result = result.filter(s -> !activeSession.equals(s));
            }

            this.plugin.dropSessions(result.collect(ImmutableCollectors.toList()));
        });
    }
}
