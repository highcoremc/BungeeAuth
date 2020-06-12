package org.nocraft.renay.bungeeauth.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.PlayerWrapper;
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

        // remove old title
        TitleBarApi.send(p.get(), "", "", 0, 10, 0);

        this.plugin.getSessionStorage().save(session)
                .thenAccept(s -> authPlayer.changeActiveSession(session))
                .thenAccept(s -> this.applySuccessfulLogin(uniqueId))
                .exceptionally(ex -> this.applyFailedLogin(ex, uniqueId));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFailedRegister(PlayerRegisterFailedEvent e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(player -> {
            String message = ChatColor.translateAlternateColorCodes('&',
                    "&c&lFAIL REGISTRATION\n" +
                            "&fWe are can not process your request\n" +
                            "&fPlease contact with administration..");
            player.disconnect(TextComponent.fromLegacyText(message));
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
