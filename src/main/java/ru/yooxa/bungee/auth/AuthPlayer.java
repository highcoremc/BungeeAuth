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


public class AuthPlayer
{
    public String mail;

    ScheduledTask task;
    String hash;

    private ProxiedPlayer player;
    private String name;
    private boolean authorize;
    private int errors;
    private long session;
    private boolean save;
    private String lastIp;

    public AuthPlayer(final ProxiedPlayer player) throws SQLException
    {
        this.player = player;
        this.name = player.getName();
        this.authorize = false;
        this.errors = 0;
        Object[] data = AuthManager.getInstance().loadData(this.player);
        if (data[0] == null) {
            this.hash = null;
            this.session = 0L;
            this.lastIp = " ";
            this.mail = " ";
        } else {
            this.hash = (String) data[0];
            this.session = (Long) data[1];
            this.lastIp = (String) data[2];
            this.mail = (String) data[3];
        }

        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable()
        {
            public void run()
            {
                if (!AuthPlayer.this.isAuth()) {
                    player.disconnect(new TextComponent("§cТы не успел войти в игру"));
                }

                AuthPlayer.this.task.cancel();
            }
        }, 2L, TimeUnit.MINUTES);
        this.task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable()
        {
            public void run()
            {
                if (!AuthPlayer.this.isAuth()) {
                    if (!AuthPlayer.this.isRegister()) {
                        player.sendMessage(new TextComponent("§eЗарегистрируйтесь - §c/register §f[§cпароль§f] [§cпароль§f]"));
                    } else {
                        player.sendMessage(new TextComponent("§eВойдите - §c/login §f[§cпароль§f]"));
                    }
                }
            }
        }, 0L, 10L, TimeUnit.SECONDS);
    }


    public ProxiedPlayer getPlayer()
    {
        return this.player;
    }


    public boolean hasMail()
    {
        return (this.mail != null && !this.mail.isEmpty());
    }


    public boolean isSave()
    {
        return this.save;
    }


    public String getLastIp()
    {
        return this.lastIp;
    }


    public long getSession()
    {
        return this.session;
    }


    public String getHash()
    {
        return this.hash;
    }


    public void logout()
    {
        this.session = -1L;
        this.player.disconnect(new TextComponent("§cВы вышли"));
    }

    public void tryAuth(String pass)
    {
        boolean hash;
        try {
            hash = PasswordSecurity.comparePasswordWithHash(pass, this.hash, this.name);
        } catch (Exception var4) {
            return;
        }

        if (hash) {
            this.authorize = true;
            this.player.sendMessage(new TextComponent("§aВы вошли в игру"));
            onLogin();
            redirect();
        } else {
            this.player.sendMessage(new TextComponent("§cНеверный пароль!"));
            this.errors++;
            if (this.errors >= 2 && this.errors < 5 && hasMail()) {
                this.player.sendMessage(new TextComponent("§eЗабыл пароль? Используй команду - §c/email recovery §f[§cВАШ_EMAIL§f]"));
            }

            if (this.errors == 5) {
                this.player.disconnect(new TextComponent("§cСлишком много попыток неудачной авторизации"));
                AuthManager.getInstance().addBlackIp(this.player.getAddress().getHostName());
            }
        }
    }


    public boolean isRegister()
    {
        return (this.hash != null);
    }


    private void onLogin()
    {
        if (Main.isMail() && !hasMail()) {
            this.player.sendMessage(new TextComponent("§eДобавьте свой email для восстановления пароля - §c/email add §f[§cВАШ_EMAIL§f] §f[§cВАШ_EMAIL§f]"));
        }
    }


    public void authSession()
    {
        this.authorize = true;
        onLogin();
    }

    public void register(String password)
    {
        if (this.hash != null) {
            this.player.sendMessage(new TextComponent("§cВы уже зарегистрированы"));
        } else {
            this.authorize = true;

            try {
                this.hash = PasswordSecurity.getHash(password, this.name);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

            this.player.sendMessage(new TextComponent("§aУспешная регистрация, не забудь свой пароль!"));
            redirect();
        }
    }

    private void redirect()
    {
        ServerInfo srv = ProxyServer.getInstance().getServerInfo(AuthManager.getInstance().getLobby());
        if (srv == null) {
            this.player.sendMessage(new TextComponent("Лобби недоступно :c"));
        } else {
            this.player.connect(srv, new Callback<Boolean>()
            {
                public void done(Boolean aBoolean, Throwable throwable)
                {
                    if (!aBoolean.booleanValue()) {
                        AuthPlayer.this.player.disconnect("§cОшибка при телепортации в Lobby");
                    }
                }
            });
        }
    }


    public void setPassword(String password)
    {
        try {
            this.hash = PasswordSecurity.getHash(password, this.name);
            this.save = true;
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }


    public boolean checkPassword(String pass)
    {
        try {
            return PasswordSecurity.comparePasswordWithHash(pass, this.hash, this.name);
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }


    public String getName()
    {
        return this.name;
    }


    public boolean isAuth()
    {
        return this.authorize;
    }
}


