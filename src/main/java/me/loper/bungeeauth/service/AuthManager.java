package me.loper.bungeeauth.service;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import me.loper.bungeeauth.BungeeAuthPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.exception.AuthenticationException;
import me.loper.bungeeauth.storage.data.SimpleDataStorage;
import me.loper.bungeeauth.storage.session.SimpleSessionStorage;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.session.Session;
import me.loper.bungeeauth.util.ImmutableCollectors;

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

            if (null == user.registeredHost) {
                user.registeredHost = conn.getVirtualHost().getHostString();
            }

            this.dataStorage.saveUser(user);

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
        String connectionHostString = c.getVirtualHost().getHostString();
        Optional<User> userQuery = this.dataStorage
            .loadUser(uniqueId).join();

        if (userQuery.isPresent()) {
            return userQuery.get();
        }

        InetSocketAddress address = (InetSocketAddress) c.getSocketAddress();
        String addressHostString = address.getHostString();
        String userName = c.getName();

        return new User(uniqueId, userName, addressHostString, connectionHostString);
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
