package org.nocraft.renay.bungee.auth.Listener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.nocraft.renay.bungee.auth.Player.AuthPlayer;
import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.Player.InvalidNicknameException;

import java.util.concurrent.TimeUnit;

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
        } catch (InvalidNicknameException ex) {
            // TODO: use message from messages.yaml file
            e.getPlayer().disconnect(new TextComponent("§cВ нике есть недопустимые символы \n§cРазрешено использовать только цифры и символы латинского алфавита"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent e)
    {
        if (e.getTarget() != null) {
            return;
        }

        AuthPlayer player = plugin.getPlayer(e.getPlayer());

        if (player == null) {
            loadPlayer(e.getPlayer());
            return;
        }

        if (player.isAuthenticated()) {
            return;
        }

        if (player.isRegistered()) {
            e.getPlayer().sendMessage(new TextComponent("§f[§c*§f] Please, authenticate with - §c/login §f[§cпароль§f]"));
        } else {
            e.getPlayer().sendMessage(new TextComponent("§f[§c*§f] Please register with - §c/register §f[§cпароль§f] [§cповтор пароля§f]"));
        }

        ServerInfo server = null;

        for (int i = 0; i < servers.size(); i++) {
            server = ProxyServer.getInstance().getServerInfo(servers.getNext());
            if (server != null) {
                break;
            }
        }

        if (server == null) {
            e.getPlayer().disconnect(new TextComponent("§cСервера аутентиикации недоступны!"));
            e.setCancelled(true);
        } else {
            e.setTarget(server);
        }
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e)
    {
        this.manager.savePlayer(e.getPlayer());
    }

    private void loadPlayer(final ProxiedPlayer p)
    {
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            try {
                AuthPlayer authPlayer = plugin.loadPlayer(p);
                if (authPlayer.getLastIp().equals(p.getAddress().getAddress().getHostAddress()) &&
                        System.currentTimeMillis() / 1000L - authPlayer.getSession() < 86400L
                ) {
                    authPlayer.authSession();
                    String lobby = Main.getInstance().manager.getLobby();
                    ServerInfo server = ProxyServer.getInstance().getServerInfo(lobby);

                    if (server == null) {
                        p.disconnect(new TextComponent("§cЛобби недоступно"));
                    } else {
                        for (int i = 0; p.getServer() == null; i++) {
                            if (i == 150) {
                                p.disconnect(new TextComponent("Auth TimeOut, Сообщите Администрации"));

                                return;
                            }
                            try {
                                Thread.sleep(100L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        p.connect(server);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                p.disconnect(new TextComponent("§cОшибка§f: Данные не загружены.\n§fСообщите Администрации \n§cvk.com/bad___guy"));
            }
        }, 0L, TimeUnit.SECONDS);
    }
}


