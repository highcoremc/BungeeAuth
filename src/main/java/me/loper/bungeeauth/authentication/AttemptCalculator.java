package me.loper.bungeeauth.authentication;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.event.PlayerAttemptsLoginExceeded;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttemptCalculator {

    private final Map<UUID, Integer> attempts = new HashMap<>();
    private final BungeeAuthPlugin plugin;
    private final int maxCountAttempts;

    public AttemptCalculator(BungeeAuthPlugin plugin, int maxCountAttempts) {
        this.plugin = plugin;
        this.maxCountAttempts = maxCountAttempts;
    }

    /**
     * Returns left attempts
     */
    public int handle(ProxiedPlayer player) {
        Integer attempts = this.attempts.getOrDefault(player.getUniqueId(), 1);

        if (attempts >= this.maxCountAttempts) {
            Event event = new PlayerAttemptsLoginExceeded(player.getUniqueId());
            this.plugin.getPluginManager().callEvent(event);
            this.clearAttempts(player.getUniqueId());
            return 0;
        }

        this.attempts.put(player.getUniqueId(), ++attempts);

        // returns attempt left
        return calcAttemptsLeft(attempts) + 1;
    }

    private int calcAttemptsLeft(Integer value) {
        return this.maxCountAttempts - value;
    }

    public void clearAttempts(UUID uniqueId) {
        this.attempts.remove(uniqueId);
    }

    public int maxAttempts() {
        return this.maxCountAttempts;
    }
}
