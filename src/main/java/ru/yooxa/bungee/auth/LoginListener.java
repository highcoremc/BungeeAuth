package ru.yooxa.bungee.auth;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LoginListener implements Listener
{
    String whitelist;

    public LoginListener(AuthManager manager)
    {
        this.whitelist = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        this.manager = manager;
        this.servers = new YIterator<>(manager.getConfig().getStringList("Login"));
    }

    private final AuthManager manager;
    private final YIterator<String> servers;

    private boolean checkMessage(String allowed, String message)
    {
        for (int i = 0; i < message.length(); i++) {
            if (!allowed.contains(String.valueOf(message.charAt(i)))) {
                return false;
            }
        }

        return true;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PreLoginEvent e)
    {
        if (e.isCancelled()) {
            return;
        }

        if (!checkMessage(this.whitelist, e.getConnection().getName())) {
            e.setCancelled(true);
            e.setCancelReason("§cВ нике есть недопустимые символы \n§cРазрешено использовать только цифры и символы латинского алфавита");

            return;
        }

        if (this.manager.botManager != null) {
            this.manager.botManager.checkClient(e);
            if (e.isCancelled()) {
                return;
            }
        }

        if (this.manager.isBlackIp(e.getConnection().getAddress().getHostString())) {
            e.setCancelled(true);
            e.setCancelReason("§cДанный IP был заблокирован на §e10 §cминут \n§cПодозрение на взлом :/");
        }
    }


    private void loadPlayer(final ProxiedPlayer p)
    {
        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
            try {
                AuthPlayer authPlayer = LoginListener.this.manager.loadPlayer(p);
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


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent e)
    {
        if (e.getTarget() != null) {
            return;
        }

        AuthPlayer player = manager.getPlayer(e.getPlayer());

        if (player == null) {
            loadPlayer(e.getPlayer());
            return;
        }

        if (player.isAuth()) {
            return;
        }

        if (player.isRegister()) {
            e.getPlayer().sendMessage(new TextComponent("§f[§c*§f] Please, authenticate with - §c/login §f[§cпароль§f]"));
        } else {
            e.getPlayer().sendMessage(new TextComponent("§f[§c*§f] Please register with - §c/register §f[§cпароль§f] [§cповтор пароля§f]"));
        }

        ServerInfo server = null;

        for (int i = 0; i < this.servers.size(); i++) {
            server = ProxyServer.getInstance().getServerInfo(this.servers.getNext());
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
}


