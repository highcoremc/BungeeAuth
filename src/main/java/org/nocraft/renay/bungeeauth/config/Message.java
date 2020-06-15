package org.nocraft.renay.bungeeauth.config;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Message {

    private final String message;

    public Message(String message) {
        this.message = message;
    }

    public BaseComponent[] asComponent(Object... objects) {
        return TextComponent.fromLegacyText(format(this.message, objects));
    }

    private static String format(String s, Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            s = s.replace("{" + i + "}", String.valueOf(o));
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    public void send(CommandSender sender, Object... objects) {
        sender.sendMessage(asComponent(objects));
    }

    public String asString(Object... objects) {
        return format(this.message, objects);
    }
}
