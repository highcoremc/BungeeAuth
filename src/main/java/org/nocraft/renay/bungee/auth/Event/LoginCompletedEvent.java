package org.nocraft.renay.bungee.auth.Event;

import org.nocraft.renay.bungee.auth.Player.AuthPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class LoginCompletedEvent extends Event {

    private final AuthPlayer player;
    private boolean cancelled;

    public LoginCompletedEvent(AuthPlayer player) {
        this.player = player;
        this.cancelled = false;
    }

    public boolean isCanceled() {
        return this.cancelled;
    }

    public void setCanceled(boolean canceled) {
        this.cancelled = canceled;
    }

    public AuthPlayer getPlayer() {
        return this.player;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return this.player.getPlayer();
    }
}
