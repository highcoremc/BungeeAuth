package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;
import org.nocraft.renay.bungeeauth.storage.entity.SimpleSessionStorage;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.session.Session;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerEnterListener extends BungeeAuthListener {

    private final Map<UUID, BungeeAuthPlayer> players;

    private final SimpleSessionStorage sessionStorage;
    private final SimpleDataStorage dataStorage;

    private final BungeeAuthPlugin plugin;

    public PlayerEnterListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.sessionStorage = plugin.getSessionStorage();
        this.dataStorage = plugin.getDataStorage();
        this.players = plugin.getAuthPlayers();
        this.plugin = plugin;
    }

    //    @EventHandler(priority = EventPriority.LOWEST)
    private void onPreLogin(PreLoginEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (!Pattern.matches("^[A-Za-z0-9_]+$", e.getConnection().getName())) {
            String msg = ChatColor.translateAlternateColorCodes('&', "&c&lFAILURE NICKNAME\n" +
                    "&fYour nickname contains forbidden symbols.\n" +
                    "&fPlease change the alias and use only the Latin alphabet\n" +
                    "with the only one acceptable character &l_");
            e.setCancelReason(TextComponent.fromLegacyText(msg));
            e.setCancelled(true);
        }
    }

    private void handleUnauthorizedAction(ServerConnectEvent e) {
        Server currentServer = e.getPlayer().getServer();

        plugin.getLogger().info("Handle unauthorized player " + e.getPlayer().getName());

        if (null != currentServer && currentServer.getInfo().getName().equals("login")) {
            e.setCancelled(true);
        }

        if (!e.getTarget().getName().equals("login")) {
            e.setTarget(this.plugin.getProxy().getServerInfo("login"));
        }
    }

    private void handlePlayerSession(ServerConnectEvent e) {
        BungeeAuthPlayer player = this.players.get(e.getPlayer().getUniqueId());

        if (null == player.session || new Date().getTime() > player.session.time.endTime.getTime()) {
            handleUnauthorizedAction(e);
            return;
        }

        if (e.getTarget() == null || "login".equals(e.getTarget().getName())) {
            e.setTarget(plugin.getProxy().getServerInfo("earth"));
        }
    }

    private User loadUser(PendingConnection c, UUID uniqueId) {
        Optional<User> userQuery = this.dataStorage
                .loadUser(uniqueId).join();

        if (userQuery.isPresent()) {
            return userQuery.get();
        }

        InetSocketAddress address = c.getAddress();

        String host = address.getHostString();
        String userName = c.getName();

        return new User(uniqueId, userName, host);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConnect(LoginEvent e) {
        InitialHandler c = (InitialHandler) e.getConnection();

        plugin.getLogger().info(String.format("Player with uuid %s has joined to the network", c.getUniqueId()));

        // TODO: enable
        // if the player is premium, we do not process him
//        if (plugin.isPremiumProfile(e.getConnection())) {
//            plugin.getLogger().info(String.format("Player %s successfully login as Premium player", c.getName()));
//            return;
//        }

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
                session.ifPresent(player::changeActiveSession);

                this.players.put(uniqueId, player);
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Exception occurred whilst loading data for " + c.getUniqueId() + " - " + c.getName());
                ex.printStackTrace();

                String message = ChatColor.translateAlternateColorCodes('&',
                        "&c&lFAILURE JOIN" +
                        "&fSorry, but the server cannot process your request.");
                e.setCancelReason(TextComponent.fromLegacyText(message));
                e.setCancelled(true);
            } finally {
                // finally, complete our intent to modify state, so the proxy can continue handling the connection.
                e.completeIntent(this.plugin);
            }
        });
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(ServerConnectEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();

        if (!this.players.containsKey(uniqueId)) {
            handleUnauthorizedAction(e);
        } else {
            handlePlayerSession(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        UUID uniqueId = p.getUniqueId();

        BungeeAuthPlayer player = this.players.remove(uniqueId);

        if (null == player) {
            this.plugin.getLogger().info(String.format("Player %s was not found in auth player HashMap", p.getName()));
            return;
        }

        // todo: save record to activity log
        // todo: on every disconnect

        if (!player.user.isRegistered()) {
            this.dataStorage.saveUser(player.user);
        }

        if (null == player.session) {
            return;
        }

        this.sessionStorage.save(player.session);
    }
}


