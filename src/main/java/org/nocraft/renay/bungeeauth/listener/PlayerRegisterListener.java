package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.event.FailedCreationSessionEvent;
import org.nocraft.renay.bungeeauth.event.PlayerLoginFailed;
import org.nocraft.renay.bungeeauth.event.PlayerSuccessfulLoginEvent;
import org.nocraft.renay.bungeeauth.event.PlayerRegisteredEvent;
import org.nocraft.renay.bungeeauth.storage.session.Session;
import org.nocraft.renay.bungeeauth.util.TitleBarApi;

import java.util.Optional;
import java.util.UUID;

public class PlayerRegisterListener extends BungeeAuthListener {

    private final BungeeAuthPlugin plugin;

    public PlayerRegisterListener(BungeeAuthPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSuccessfulRegister(PlayerRegisteredEvent e) {
        UUID uniqueId = e.getPlayerId();
        Optional<ProxiedPlayer> p = plugin.getPlayer(e.getPlayerId());

        if (!p.isPresent()) {
            plugin.getLogger().info("Player " + uniqueId + " is not present on the server..");
            return;
        }

        Optional<Session> optionalSession = this.plugin.getAuthFactory().createSession(uniqueId);

        if (!optionalSession.isPresent()) {
            plugin.getLogger().info("Player session " + p.get().getName() + " is not present..");
            this.plugin.getPluginManager().callEvent(
                    new FailedCreationSessionEvent(uniqueId));
            return;
        }

        Session session = optionalSession.get();
        BungeeAuthPlayer authPlayer = this.plugin
                .getAuthPlayers()
                .get(uniqueId);

        plugin.getLogger().info("Successful handled register for player " + p.get().getName());

        // remove old title
        TitleBarApi.send(p.get(), "", "", 0, 10, 0);

        this.plugin.getSessionStorage().save(session)
                .thenAccept(s -> authPlayer.changeActiveSession(session))
                .thenAccept(s -> this.applySuccessfulLogin(uniqueId))
                .exceptionally(ex -> this.applyFailedLogin(ex, uniqueId));
    }

    private Void applyFailedLogin(Throwable ex, UUID uniqueId) {
        ex.printStackTrace();
        this.plugin.getPluginManager().callEvent(new PlayerLoginFailed(uniqueId));
        return null;
    }

    private void applySuccessfulLogin(UUID uniqueId) {
        this.plugin.getPluginManager().callEvent(new PlayerSuccessfulLoginEvent(uniqueId));
    }
}
