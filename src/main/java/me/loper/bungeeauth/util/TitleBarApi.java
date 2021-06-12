package me.loper.bungeeauth.util;


import me.loper.bungeeauth.config.Message;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TitleBarApi {

    private final int fadeOut;
    private final int fadeIn;
    private final int stay;

    public TitleBarApi(int fadeIn, int stay, int fadeOut) {
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.stay = stay;
    }

    public void send(ProxiedPlayer player, Message title, Message subtitle) {
        send(player, title, subtitle, this.fadeIn, this.stay, this.fadeOut);
    }

    public static void send(ProxiedPlayer proxiedPlayer, Message title, Message subtitle, int fadeIn, int stay, int fadeOut) {
        Title t = ProxyServer.getInstance().createTitle()
            .title(title.asComponent())
            .subTitle(subtitle.asComponent());
        t.fadeIn(fadeIn).stay(stay).fadeOut(fadeOut);
        t.send(proxiedPlayer);
    }
}
