package org.nocraft.renay.bungeeauth;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;

public class PlayerWrapper {

    private final ProxiedPlayer player;

    public PlayerWrapper(ProxiedPlayer p) {
        this.player = p;
    }

    public static PlayerWrapper wrap(ProxiedPlayer p) {
        return new PlayerWrapper(p);
    }

    public String getIpAddress() {
        return ((InetSocketAddress) player.getSocketAddress()).getHostString();
    }
}
