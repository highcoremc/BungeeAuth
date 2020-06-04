package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuth;
import org.nocraft.renay.bungeeauth.exception.InvalidNicknameException;
import org.nocraft.renay.bungeeauth.user.Session;
import org.nocraft.renay.bungeeauth.user.SessionTime;
import org.nocraft.renay.bungeeauth.user.User;
import org.nocraft.renay.bungeeauth.storage.data.SimpleDataStorage;

import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

public class LoginListener implements Listener {

    private final BungeeAuth plugin;
    private final SimpleDataStorage storage;

    public LoginListener(BungeeAuth plugin, SimpleDataStorage storage) {
        this.storage = storage;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PreLoginEvent e) {
        PendingConnection c = e.getConnection();

        try {
            this.connectionValidate(c);
        } catch(RuntimeException ex) {
            c.disconnect(TextComponent.fromLegacyText(ChatColor.RED + ex.getMessage()));
        }

        e.registerIntent(this.plugin);

        // load player from database
        this.plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                User u = this.saveIfUnique(c.getUniqueId(), c.getName(), c.getAddress());
            } catch (Exception ex) {
                this.plugin.getLogger().severe("Exception occurred whilst loading data for " + c.getUniqueId() + " - " + c.getName());
                ex.printStackTrace();

                e.setCancelReason(TextComponent.fromLegacyText(ChatColor.RED + "Sorry, but the server cannot process your request."));
                e.setCancelled(true);
            }

            // finally, complete our intent to modify state, so the proxy can continue handling the connection.
            e.completeIntent(this.plugin);
        });
    }

    private User saveIfUnique(UUID uniqueId, String name, InetSocketAddress address) {
        User user = this.storage.loadUser(uniqueId).join();

        if (user != null) {
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.SECOND, 86400);

            user = new User(uniqueId, name);

            SessionTime time = new SessionTime(startTime.getTime(), endTime.getTime());
            Session session = new Session(user, time, address.getHostString());

            user.changeActiveSession(session);
        }

        return user;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent e) {
        if (e.getTarget() != null) {
            return;
        }
        // check user authentication, if him authenticated - connect user to one of lobby or another server
        // else - connect user to login server and ask him password
    }

    private void connectionValidate(PendingConnection c) {
        if (!Pattern.matches("/^[A-Za-z0-9_]+$/", c.getName())) {
            throw new InvalidNicknameException();
        }
    }
}


