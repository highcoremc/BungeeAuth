package ru.yooxa.bungee.auth;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class CommandEvent extends Event
{
    private final ProxiedPlayer player;
    private final String command;
    private final String[] args;
    private boolean used;
    private boolean cancelled;

    public CommandEvent(ProxiedPlayer player, String command, String[] args)
    {
        this.player = player;
        this.command = command;
        this.args = args;
        this.cancelled = false;
    }

    public boolean isCanceled()
    {
        return this.cancelled;
    }

    public void setCanceled(boolean canceled)
    {
        this.cancelled = canceled;
    }

    public boolean isUsed()
    {
        return this.used;
    }

    public void used()
    {
        this.used = true;
    }

    public ProxiedPlayer getPlayer()
    {
        return this.player;
    }

    public String getCommand()
    {
        return this.command;
    }

    public String[] getArgs()
    {
        return this.args;
    }
}


