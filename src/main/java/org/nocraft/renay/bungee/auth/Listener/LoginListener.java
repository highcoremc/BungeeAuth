package org.nocraft.renay.bungee.auth.Listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.Player.AuthPlayer;

public class LoginListener implements Listener
{
    private final BungeeAuth plugin;

    public LoginListener(BungeeAuth plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PostLoginEvent e)
    {
        try {
            AuthPlayer.validate(e.getPlayer());
        } catch (Exception ex) {
            // TODO: use message from messages.yaml file
            e.getPlayer().disconnect(new TextComponent("§cВ нике есть недопустимые символы \n§cРазрешено использовать только цифры и символы латинского алфавита"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent e) {
        if (e.getTarget() != null) {
            return;
        }
    }
}


