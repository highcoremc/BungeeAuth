package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.SessionFactory;
import org.nocraft.renay.bungeeauth.exception.InvalidNicknameException;
import org.nocraft.renay.bungeeauth.storage.entity.SimpleSessionStorage;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;
import org.nocraft.renay.bungeeauth.storage.entity.SessionTime;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class LoginListener implements Listener {

    private final ConcurrentHashMap<UUID, Session> pendingConnections = new ConcurrentHashMap<UUID, Session>();

    private final SimpleSessionStorage sessionStorage;
    private final SimpleDataStorage dataStorage;

    private final BungeeAuthPlugin plugin;

    public LoginListener(BungeeAuthPlugin plugin, SimpleDataStorage dataStorage, SimpleSessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(LoginEvent e) {
        InitialHandler c = (InitialHandler) e.getConnection();

        plugin.getLogger().info(String.format("Player with uuid %s has joined to the network", c.getUniqueId()));

        // if the player is premium, we do not process him
        if (plugin.isPremiumProfile(e.getConnection())) {
            plugin.getLogger().info(String.format("Player %s successfully login as Premium player", c.getName()));
            return;
        }

        e.registerIntent(this.plugin);

        // load player from database
        this.plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                UUID uniqueId = c.getUniqueId();

                Optional<Session> session = this.sessionStorage.loadSession(uniqueId).join();
                session.orElse(this.plugin.getSessionFactory().create(c));

                //noinspection OptionalGetWithoutIsPresent
                this.pendingConnections.put(uniqueId, session.get());
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Exception occurred whilst loading data for " + c.getUniqueId() + " - " + c.getName());
                ex.printStackTrace();

                e.setCancelReason(TextComponent.fromLegacyText(ChatColor.RED + "Sorry, but the server cannot process your request."));
                e.setCancelled(true);
            } finally {
                // finally, complete our intent to modify state, so the proxy can continue handling the connection.
                e.completeIntent(this.plugin);
            }
        });
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(PostLoginEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();

        if (!this.pendingConnections.containsKey(uniqueId)) {
            return;
        }

        Session session = this.pendingConnections.get(uniqueId);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(ServerConnectEvent e) {
        ProxiedPlayer p = e.getPlayer();

        // Check if the target equals login server
        // then skip if true
        if ("login".equals(e.getTarget().getName())) {
            return;
        }

        // check if the player has authenticated - and pass him to target
        // else - send him to the login server and sent title with helps message.
    }

    private void connectionValidate(PendingConnection c) {
        if (!Pattern.matches("/^[A-Za-z0-9_]+$/", c.getName())) {
            throw new InvalidNicknameException();
        }
    }
}


