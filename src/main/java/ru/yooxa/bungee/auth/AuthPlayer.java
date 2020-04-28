package ru.yooxa.bungee.auth;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import ru.yooxa.bungee.auth.hash.PasswordSecurity;


public class AuthPlayer {
    public String mail;

    ScheduledTask task;
    String hash;

    private final ProxiedPlayer player;
    private final String lastIp;
    private final String name;
    private boolean authorize;
    private int errors;
    private long session;
    private boolean save;

    public AuthPlayer(final ProxiedPlayer p) throws SQLException {
        player = p;
        name = player.getName();
        authorize = false;
        errors = 0;
        Object[] data = AuthManager.getInstance().loadData(player);
        if (data[0] == null) {
            hash = null;
            session = 0L;
            lastIp = " ";
            mail = " ";
        } else {
            hash = (String) data[0];
            session = (Long) data[1];
            lastIp = (String) data[2];
            mail = (String) data[3];
        }

        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
            public void run() {
                if (!isAuth()) {
                    player.disconnect(new TextComponent("§cТы не успел войти в игру"));
                }

                task.cancel();
            }
        }, 2L, TimeUnit.MINUTES);
        task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
            if (!isAuth()) {
                if (!isRegister()) {
                    player.sendMessage(new TextComponent("§eЗарегистрируйтесь - §c/register §f[§cпароль§f] [§cпароль§f]"));
                } else {
                    player.sendMessage(new TextComponent("§eВойдите - §c/login §f[§cпароль§f]"));
                }
            }
        }, 0L, 10L, TimeUnit.SECONDS);
    }


    public ProxiedPlayer getPlayer() {
        return player;
    }


    public boolean hasMail() {
        return (mail != null && !mail.isEmpty());
    }


    public boolean isSave() {
        return save;
    }


    public String getLastIp() {
        return lastIp;
    }


    public long getSession() {
        return session;
    }


    public String getHash() {
        return hash;
    }


    public void logout() {
        session = -1L;
        player.disconnect(new TextComponent("§cВы вышли"));
    }

    public void tryAuth(String pass) {
        boolean hash;
        try {
            hash = PasswordSecurity.comparePasswordWithHash(pass, this.hash, name);
        } catch (Exception var4) {
            return;
        }

        if (hash) {
            authorize = true;
            player.sendMessage(new TextComponent("§aВы вошли в игру"));
            onLogin();
            redirect();
        } else {
            player.sendMessage(new TextComponent("§cНеверный пароль!"));
            errors++;
            if (errors >= 2 && errors < 5 && hasMail()) {
                player.sendMessage(new TextComponent("§eЗабыл пароль? Используй команду - §c/email recovery §f[§cВАШ_EMAIL§f]"));
            }

            if (errors == 5) {
                player.disconnect(new TextComponent("§cСлишком много попыток неудачной авторизации"));
                AuthManager.getInstance().addBlackIp(player.getAddress().getHostName());
            }
        }
    }


    public boolean isRegister() {
        return (hash != null);
    }


    private void onLogin() {
        if (Main.isMail() && !hasMail()) {
            player.sendMessage(new TextComponent("§eДобавьте свой email для восстановления пароля - §c/email add §f[§cВАШ_EMAIL§f] §f[§cВАШ_EMAIL§f]"));
        }
    }


    public void authSession() {
        authorize = true;
        onLogin();
    }

    public void register(String password) {
        if (hash != null) {
            player.sendMessage(new TextComponent("§cВы уже зарегистрированы"));
        } else {
            authorize = true;

            try {
                hash = PasswordSecurity.getHash(password, name);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

            player.sendMessage(new TextComponent("§aУспешная регистрация, не забудь свой пароль!"));
            redirect();
        }
    }

    private void redirect() {
        ServerInfo srv = ProxyServer.getInstance().getServerInfo(AuthManager.getInstance().getLobby());
        if (srv == null) {
            player.sendMessage(new TextComponent("Лобби недоступно :c"));
        } else {
            player.connect(srv, new Callback<Boolean>() {
                public void done(Boolean aBoolean, Throwable throwable) {
                    if (!aBoolean) {
                        player.disconnect("§cОшибка при телепортации в Lobby");
                    }
                }
            });
        }
    }


    public void setPassword(String password) {
        try {
            hash = PasswordSecurity.getHash(password, name);
            save = true;
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }


    public boolean checkPassword(String pass) {
        try {
            return PasswordSecurity.comparePasswordWithHash(pass, hash, name);
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }


    public String getName() {
        return name;
    }


    public boolean isAuth() {
        return authorize;
    }
}


