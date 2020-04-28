package ru.yooxa.bungee.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class Database {
    private final AuthManager manager;

    public Database(AuthManager manager) {
        this.manager = manager;
        connect();
    }

    private Connection connection;

    private String getString(String name) {
        return this.manager.getConfig().getString("Table." + name);
    }


    public void connect() {
        Configuration config = this.manager.getConfig();

        try {
            this.connection = DriverManager.getConnection("JDBC:mysql://" + config.getString("Auth.host") + "/" + config.getString("Auth.database") + "?useUnicode=true&characterEncoding=utf-8", config.getString("Auth.username"), config.getString("Auth.password"));
            Statement ex = this.connection.createStatement();
            ex.executeUpdate("CREATE TABLE IF NOT EXISTS `" + getString("tablename") + "` (" + "`id` INTEGER AUTO_INCREMENT PRIMARY KEY, " + "`" + getString("username") + "` VARCHAR(50) NOT NULL UNIQUE, " + "`" + getString("password") + "` VARCHAR(255) NOT NULL, " + "`" + getString("ip") + "` VARCHAR(50), " + "`" + getString("session") + "` LONG, " + "`" + getString("email") + "` VARCHAR(50) DEFAULT '', " + "`" + getString("server") + "` VARCHAR(50) DEFAULT '')");

            try {
                ex.executeUpdate("ALTER TABLE `" + getString("tablename") + "` ADD `server` VARCHAR(50) default ''");
                ex.executeUpdate("ALTER TABLE `" + getString("tablename") + "` ALTER COLUMN `" + getString("email") + "` SET DEFAULT ''");
            } catch (SQLException sQLException) {
            }


            ex.close();
            Main.getInstance().getLogger().info("[Auth] Соединение с базой данных установлено");
        } catch (SQLException var5) {
            var5.printStackTrace();
            Main.getInstance().getLogger().info("Ошибка при соединении с базой данных. Ошибка - " + var5.getMessage());
        }
    }


    private Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            connect();
        }

        return this.connection;
    }

    public Object[] loadData(String name) throws SQLException {
        long start = System.currentTimeMillis();
        Object[] data = {null, Integer.valueOf(0), null, null};
        Statement statement = getConnection().createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM `" + getString("tablename") + "` WHERE `" + getString("username") + "`='" + name.toLowerCase() + "'");
        if (rs.next()) {
            data[0] = rs.getString(getString("password"));
            data[1] = Long.valueOf(rs.getLong(getString("session")));
            data[2] = rs.getString(getString("ip"));
            data[3] = rs.getString(getString("email"));
        }

        rs.close();
        statement.close();
        Main.getInstance().getLogger().info("#Auth | Player '" + name + "' loaded (" + (System.currentTimeMillis() - start) + ") ms");
        return data;
    }


    public Object[] loadData(ProxiedPlayer player) throws SQLException {
        return loadData(player.getName());
    }


    public void execute(String sql) throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql);
        }
    }


    public void saveData(final AuthPlayer player, boolean saveSession) {
        final String query = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES ('%s','%s','%s','%s','%s','%s') ON DUPLICATE KEY UPDATE `%2$s`='%8$s',`%3$s`='%9$s',`%4$s`='%10$s',`%5$s`='%11$s',`%6$s`='%12$s',`%7$s`='%13$s';", new Object[]{


                getString("tablename"), getString("username"), getString("password"), getString("session"), getString("ip"), getString("server"), getString("email"), player.getName().toLowerCase(), player.getHash(), String.valueOf((player.getSession() == -1L || !saveSession) ? 0L : (System.currentTimeMillis() / 1000L)), player.getPlayer().getAddress().getAddress().getHostAddress(), (player.getPlayer().getServer() == null) ? "" : player.getPlayer().getServer().getInfo().getName(), player.mail});

        Main.getInstance().getProxy().getScheduler().runAsync(Main.getInstance(), new Runnable() {
            public void run() {
                try {
                    Statement statement = Database.this.getConnection().createStatement();
                    statement.executeUpdate(query);
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Main.getInstance().getLogger().info(String.format("Ошибка при сохранении данных игрока '%s': %s", new Object[]{player.getName(), e.getMessage()}));
                }
            }
        });
    }
}


