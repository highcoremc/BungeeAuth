package ru.yooxa.bungee.auth;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import ru.yooxa.bungee.auth.antibot.BotManager;
import ru.yooxa.bungee.auth.hash.PasswordSecurity;

public class AuthManager
{
    private static AuthManager instance;
    public IpProtect ipProtect;
    public Configuration config;

    public List<String> admins;
    public BotManager botManager;
    private final Main main;
    private final HashMap<String, AuthPlayer> players = new HashMap<>();
    private final Set<String> blacklist = new HashSet<>();
    private final YIterator<String> lobbyIterator;
    private final Database database;

    public AuthManager(Main main)
    {
        instance = this;
        this.main = main;

        loadConfig();

        this.lobbyIterator = new YIterator<>(getConfig().getStringList("Lobby"));
        this.admins = getConfig().getStringList("Admins");
        this.ipProtect = new IpProtect(this);
        this.database = new Database(this);

        main.getProxy().getPluginManager().registerListener(main, new ChatListener(this));
        main.getProxy().getPluginManager().registerListener(main, new LoginListener(this));

        if (getConfig().getBoolean("AntiBot.enable")) {
            this.botManager = new BotManager(this);
        }
    }

    public static AuthManager getInstance()
    {
        return instance;
    }

    public Configuration getConfig()
    {
        return this.config;
    }

    public AuthPlayer loadPlayer(ProxiedPlayer player) throws SQLException
    {
        AuthPlayer authPlayer = new AuthPlayer(player);
        this.players.put(player.getName().toLowerCase(), authPlayer);
        return authPlayer;
    }

    public String getLobby()
    {
        return (String) this.lobbyIterator.getNext();
    }

    public void savePlayer(ProxiedPlayer player)
    {
        AuthPlayer auth = getPlayer(player);
        if (auth != null) {
            this.players.remove(player.getName().toLowerCase());

            if (auth.isSave() || auth.isAuth()) {
                this.database.saveData(auth, auth.isAuth());
            }
        }
    }

    public AuthPlayer getPlayer(ProxiedPlayer player)
    {
        return (AuthPlayer) this.players.get(player.getName().toLowerCase());
    }

    public Object[] loadData(ProxiedPlayer player) throws SQLException
    {
        return this.database.loadData(player);
    }

    public boolean isBlackIp(String ip)
    {
        return this.blacklist.contains(ip);
    }

    public void addBlackIp(final String ip)
    {
        this.blacklist.add(ip);
        this.main.getProxy().getScheduler().schedule(this.main, () -> AuthManager.this.blacklist.remove(ip), 10L, TimeUnit.MINUTES);
    }

    public void loadConfig()
    {
        try {
            if (!this.main.getDataFolder().exists()) {
                this.main.getDataFolder().mkdir();
            }

            File file = new File(this.main.getDataFolder(), "auth.yml");
            ConfigurationProvider provider = ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class);

            if (!file.exists()) {
                provider.save(provider.load(this.main.getResourceAsStream("auth.yml")), file);
            }

            this.config = provider.load(file);

            addDefault("hash", "SHA256");
            addDefault("mail", Boolean.TRUE);

            provider.save(this.config, file);
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    private void addDefault(String key, Object value)
    {
        if (!this.config.getKeys().contains(key)) this.config.set(key, value);
    }

    public void register(String name, String password) throws Exception
    {
        String hash = PasswordSecurity.getHash(password, name);
        String sql = "INSERT INTO `authme` (`username`, `password`, `session`, `ip`, `server`, `email`) VALUES ('" + name.toLowerCase() + "'," + "'" + hash + "'," + "'0'," + "'1.2.3.4'," + "''," + "'')";
        this.database.execute(sql);
    }

    public void unregister(String name)
    {
        try {
            database.execute("DELETE FROM `authme` WHERE `username`='" + name + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changePassword(String name, String newPassword) throws Exception
    {
        String hash = PasswordSecurity.getHash(newPassword, name);
        this.database.execute("UPDATE `authme` SET `password`='" + hash + "'  WHERE `username`='" + name + "' ");
    }

    public void changeMail(String name, String newMail) throws Exception
    {
        this.database.execute("UPDATE `authme` SET `email`='" + newMail + "'  WHERE `username`='" + name + "' ");
    }

    public long lastLogin(String name) throws SQLException
    {
        Object[] data = this.database.loadData(name);
        return (Long) data[1];
    }
}


