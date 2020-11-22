package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.event.PlayerAuthenticatedEvent;
import org.nocraft.renay.bungeeauth.server.ServerManager;
import org.nocraft.renay.bungeeauth.server.ServerType;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;
import org.nocraft.renay.bungeeauth.storage.entity.SimpleSessionStorage;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.session.Session;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Pattern;

public class PlayerEnterListener extends BungeeAuthListener {

    private final SimpleSessionStorage sessionStorage;
    private final SimpleDataStorage dataStorage;

    private final ServerManager connector;
    private final BungeeAuthPlugin plugin;

    public PlayerEnterListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.sessionStorage = plugin.getSessionStorage();
        this.connector = plugin.getServerManager();
        this.dataStorage = plugin.getDataStorage();
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent e) {
        if (e.isCancelled() || null == e.getConnection().getName()) {
            return;
        }

        String protocolRegex = this.plugin.getConfiguration()
                .get(ConfigKeys.PROTOCOL_REGEX);
        PendingConnection conn = e.getConnection();
        String version = String.valueOf(conn.getVersion());

        if (!Pattern.matches(protocolRegex, version)) {
            e.setCancelled(true);
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.VERSION_OUTDATED);
            e.setCancelReason(message.asComponent());
            return;
        }

        String userName = conn.getName();

        if (!Pattern.matches("^[A-Za-z0-9_]+$", userName)) {
            e.setCancelled(true);
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.BAD_NICKNAME);
            e.setCancelReason(message.asComponent());
        }

        InetSocketAddress address = (InetSocketAddress) conn.getSocketAddress();

        try {
            this.checkUserInWhitelist(userName, address.getHostString());
        } catch (RuntimeException ex) {
            this.plugin.getLogger().warning(String.format(
                "User with ip `%s` tried to login by username %s", address.getHostString(), userName));

            e.setCancelled(true);

            Message message = plugin.getMessageConfig()
                .get(MessageKeys.FORBIDDEN_ACCESS);
            e.setCancelReason(message.asComponent());
        }
    }

    private void checkUserInWhitelist(String userName, String userIp) {
        Map<String, List<String>> whiteList = this.plugin.getConfiguration()
            .get(ConfigKeys.WHITELIST_USERS);

        List<String> allowedIps = whiteList.get(userName);

        if (null == allowedIps) {
            return;
        }

        if (allowedIps.contains(userIp)) {
            return;
        }

        throw new RuntimeException("This ip address is not allowed for this user.");
    }

    private void handleUnauthorizedAction(ServerConnectEvent e) {
        // current server player
        Server server = e.getPlayer().getServer();

        plugin.getLogger().info("Handle unauthorized player " + e.getPlayer().getName());

        if (null == server) {
            try {
                e.setTarget(this.connector.getServer(ServerType.LOGIN));
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                this.connector.disconnect(e.getPlayer());
            }
            return;
        }

        ServerType serverType = this.connector.getServerType(server.getInfo());
        if (serverType.equals(ServerType.LOGIN)) {
            e.setCancelled(true);
        }

        if (!this.connector.getServerType(e.getTarget()).equals(ServerType.LOGIN)) {
            try {
                e.setTarget(this.connector.getServer(ServerType.LOGIN));
            } catch (IllegalStateException ex) {
                this.connector.disconnect(e.getPlayer());
                ex.printStackTrace();
            }
        }
    }

    private void handlePlayerSession(ServerConnectEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        BungeeAuthPlayer player = this.plugin.getAuthPlayers().get(uniqueId);

        if (null == player.session || new Date().getTime() > player.session.time.endTime.getTime()) {
            handleUnauthorizedAction(e);
            return;
        }

        ServerType target = connector.getServerType(e.getTarget());

        if (target.equals(ServerType.LOGIN)) {
            try {
                e.setTarget(this.connector.getServer(ServerType.GAME));
            } catch (IllegalStateException ex) {
                connector.disconnect(e.getPlayer());
                ex.printStackTrace();
                return;
            }
        }

        Event event = new PlayerAuthenticatedEvent(uniqueId, true);
        this.plugin.getPluginManager().callEvent(event);
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(LoginEvent e) {
        PendingConnection c = e.getConnection();

        plugin.getLogger().info(String.format("Player with uuid %s has joined to the network", c.getUniqueId()));

        e.registerIntent(this.plugin);

        // load session for player from database
        this.plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                UUID uniqueId = c.getUniqueId();
                User user = loadUser(c, uniqueId);

                String host = c.getAddress().getHostString();
                BungeeAuthPlayer player = new BungeeAuthPlayer(user);

                Optional<Session> session = this.sessionStorage
                        .loadSession(uniqueId, host).join();
                if (!player.user.isRegistered()) {
                    session.ifPresent(s -> this.sessionStorage.remove(uniqueId, s.ipAddress));
                } else {
                    session.ifPresent(player::changeActiveSession);
                }

                this.plugin.getAuthPlayers().put(uniqueId, player);
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Exception occurred whilst loading data for " + c.getUniqueId() + " - " + c.getName());
                ex.printStackTrace();
                e.setCancelled(true);
                Message message = plugin.getMessageConfig()
                        .get(MessageKeys.BAD_REQUEST);
                e.setCancelReason(message.asComponent());
            } finally {
                // finally, complete our intent to modify state, so the proxy can continue handling the connection.
                e.completeIntent(this.plugin);
            }
        });
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(ServerConnectEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();

        if (!this.plugin.getAuthPlayers().containsKey(uniqueId)) {
            handleUnauthorizedAction(e);
            return;
        }

        BungeeAuthPlayer player = this.plugin
                .getAuthPlayers()
                .get(uniqueId);
        if (!player.isAuthenticated()) {
            handlePlayerSession(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        UUID uniqueId = p.getUniqueId();
        this.plugin.getAuthPlayers().remove(uniqueId);
    }
}


