package me.loper.bungeeauth.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import me.loper.bungeeauth.authentication.AttemptCalculator;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.event.PlayerAttemptsLoginExceeded;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerBanListener extends BungeeAuthListener {

    private final Map<UUID, Long> bannedPlayers = new ConcurrentHashMap<>();

    private final BungeeAuthPlugin plugin;
    private final int bannedTime;

    public PlayerBanListener(BungeeAuthPlugin plugin, int bannedTime) {
        super(plugin);
        this.plugin = plugin;
        this.bannedTime = bannedTime;
        plugin.getScheduler().asyncRepeating(
                this::cleanup, 15, TimeUnit.SECONDS);
    }

    private void cleanup() {
        Iterator<Map.Entry<UUID, Long>> iterator = this.bannedPlayers
                .entrySet().iterator();

        while (iterator.hasNext()) {
            Long endTime = iterator.next().getValue();

            if (System.currentTimeMillis() >= endTime) {
                iterator.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        UUID uniqueId = player.getUniqueId();

        AttemptCalculator attemptCalculator = this.plugin.getAttemptCalculator();
        if (this.bannedPlayers.containsKey(uniqueId)) {
            int maxAttempts = attemptCalculator.maxAttempts();
            long banTime = this.bannedPlayers.get(uniqueId);
            long timeLeftMinutes = getTimeLeftMinutes(banTime);

            Message message = plugin.getMessageConfig().get(MessageKeys.TEMPORARY_FORBIDDEN_ACCESS);
            player.disconnect(message.asComponent(timeLeftMinutes, maxAttempts));
        }
    }

    public long getTimeLeftMinutes(long entry) {
        long now = System.currentTimeMillis();
        return (entry - now) / 1000 / 60;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerExceededAttempts(PlayerAttemptsLoginExceeded e) {
        this.plugin.getPlayer(e.getPlayerId()).ifPresent(this::banPlayer);
    }

    private void banPlayer(ProxiedPlayer p) {
        // end time of ban in minutes represent in milliseconds
        long endTime = System.currentTimeMillis() + (1000L * 60 * bannedTime);
        this.bannedPlayers.put(p.getUniqueId(), endTime);

        Message message = plugin.getMessageConfig().get(
            MessageKeys.EXCEEDED_LOGIN_ATTEMPTS);

        p.disconnect(message.asComponent());
    }
}
