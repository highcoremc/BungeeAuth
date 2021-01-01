package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.PlayerWrapper;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.event.*;
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

        ProxiedPlayer player = p.get();
        Optional<Session> optionalSession = this.plugin.getAuthFactory().createSession(uniqueId);
        if (!optionalSession.isPresent()) {
            plugin.getLogger().info("Player session " + player.getName() + " is not present..");
            this.plugin.getPluginManager().callEvent(new FailedCreationSessionEvent(uniqueId));
            return;
        }

        Session session = optionalSession.get();
        BungeeAuthPlayer authPlayer = this.plugin.getAuthPlayer(uniqueId);

        // remove old title
        TitleBarApi.send(player, new Message(""), new Message(""), 0, 10, 0);
        this.plugin.getSessionStorage().save(session)
                .thenAccept(s -> authPlayer.changeActiveSession(session))
                .thenAccept(s -> this.applySuccessfulRegister(player))
                .thenAccept(s -> this.applySuccessfulLogin(uniqueId))
                .exceptionally(ex -> this.applyFailedLogin(ex, uniqueId));
    }

    public void applySuccessfulRegister(ProxiedPlayer p) {
        String message = "Player %s was a successfully registered!";
        plugin.getLogger().info(String.format(message, p.getName()));
        this.plugin.getAttemptManager().clearAttempts(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFailedRegister(PlayerRegisterFailedEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            Message message = plugin.getMessageConfig()
                .get(MessageKeys.FAILED_REGISTER);

            player.disconnect(message.asComponent());
            String msg = "Player %s failed register with ip %s.";
            plugin.getLogger().warning(String.format(msg, player.getName(), PlayerWrapper.wrap(player).getIpAddress()));
        });
    }

    private Void applyFailedLogin(Throwable ex, UUID uniqueId) {
        this.plugin.getPluginManager().callEvent(new PlayerLoginFailedEvent(uniqueId));
        ex.printStackTrace();
        return null;
    }

    private void applySuccessfulLogin(UUID uniqueId) {
        this.plugin.getPluginManager().callEvent(new PlayerAuthenticatedEvent(uniqueId));
    }
}
