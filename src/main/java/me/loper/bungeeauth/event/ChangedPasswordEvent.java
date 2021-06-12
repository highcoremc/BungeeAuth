package me.loper.bungeeauth.event;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

public class ChangedPasswordEvent extends Event {

    private final CommandSender sender;
    private final String playerName;
    private final UUID playerId;

    public ChangedPasswordEvent(CommandSender sender, UUID playerId, String playerName) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.sender = sender;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public boolean isProxiedPlayer() {
        return this.sender instanceof ProxiedPlayer;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
