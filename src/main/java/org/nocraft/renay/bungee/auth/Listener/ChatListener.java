package org.nocraft.renay.bungee.auth.Listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungee.auth.Player.AuthPlayer;
import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.Event.CommandEvent;

public class ChatListener implements Listener {

    String whitelist = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_¸abcdefghijklmnopqrstuvwxyz{|}~АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЫЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    private final BungeeAuth plugin;

    public ChatListener(BungeeAuth plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) e.getSender();
        AuthPlayer player = plugin.getPlayer(p);

        if (null == player) {
            e.setCancelled(true);
            return;
        }

        if (!e.getMessage().startsWith("/") && !player.isAuthenticated()) {
            e.setCancelled(true);
            return;
        }

        String[] message = e.getMessage().split(" ");
        String command = message[0].replaceFirst("/", "");
        String[] args = new String[message.length - 1];
        System.arraycopy(message, 1, args, 0, message.length - 1);

        CommandEvent event = new CommandEvent((ProxiedPlayer) e.getSender(), command, args);

        plugin.getProxy().getPluginManager().callEvent((Event) event);

        if (event.isUsed()) {
            e.setCancelled(true);
        }
    }
}