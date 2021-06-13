package me.loper.bungeeauth.storage.data.implementation;

import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.authentication.hash.HashMethodType;
import me.loper.bungeeauth.storage.data.DataStorage;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.UserPassword;
import me.loper.storage.sql.SchemaReader;
import me.loper.storage.sql.connection.factory.HikariConnectionFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataSqlStorage implements DataStorage {

    private static final String USER_SELECT_ID_BY_USERNAME = "SELECT id FROM '{prefix}players' WHERE username=? LIMIT 1";
    private static final String USER_SELECT_USERNAME_BY_ID = "SELECT username FROM '{prefix}players' WHERE unique_id=? LIMIT 1";
    private static final String USER_INSERT = "INSERT INTO '{prefix}users' (unique_id, username, realname, registered_ip) VALUES(?, ?, ?, ?)";
    private static final String USER_SELECT_BY_UID = "SELECT unique_id,username,realname,registered_at,registered_ip FROM '{prefix}users' WHERE unique_id=?";
    private static final String USER_SELECT_BY_NAME = "SELECT unique_id,username,realname,registered_at,registered_ip FROM '{prefix}users' WHERE username=?";
    private static final String USER_SELECT_ID_BY_UUID = "SELECT id FROM '{prefix}users' WHERE unique_id=?";
    private static final String USER_SELECT_ALL_IDS = "SELECT id FROM '{prefix}users'";

    private static final String USER_PASSWORD_SELECT_ID = "SELECT id FROM '{prefix}user_password' WHERE unique_id = ?";
    private static final String USER_PASSWORD_SELECT = "SELECT unique_id,password,hash_method_type,updated_at,created_at FROM '{prefix}user_password' WHERE unique_id = ?";
    private static final String USER_PASSWORD_INSERT = "INSERT INTO '{prefix}user_password' (unique_id, password, hash_method_type) VALUES (?, ?, ?)";
    private static final String USER_PASSWORD_UPDATE = "UPDATE '{prefix}user_password' SET password = ?, hash_method_type = ? WHERE unique_id = ?";

    private final BungeeAuthPlugin plugin;

    private final HikariConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public DataSqlStorage(BungeeAuthPlugin plugin, HikariConnectionFactory connectionFactory, String tablePrefix) {
        this.plugin = plugin;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() throws Exception {
        this.connectionFactory.init();

        boolean tableExists;
        try (java.sql.Connection c = this.connectionFactory.getConnection()) {
            tableExists = tableExists(c, this.statementProcessor.apply("{prefix}users"));
        }

        if (!tableExists) {
            applySchema();
        }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
            while (rs.next()) {
                if (rs.getString(3).equalsIgnoreCase(table)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void applySchema() throws IOException, SQLException {
        List<String> statements;

        String schemaFileName = "me/loper/bungeeauth/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";

        try (InputStream is = this.plugin.getResourceStream(schemaFileName)) {
            if (is == null) {
                throw new IOException("Couldn't locate schema file for " + this.connectionFactory.getImplementationName());
            }

            statements = SchemaReader.getStatements(is).stream()
                    .map(this.statementProcessor)
                    .collect(Collectors.toList());
        }

        try (Connection connection = this.connectionFactory.getConnection()) {
            boolean utf8mb4Unsupported = false;

            try (Statement s = connection.createStatement()) {
                for (String query : statements) {
                    s.addBatch(query);
                }

                try {
                    s.executeBatch();
                } catch (BatchUpdateException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        utf8mb4Unsupported = true;
                    } else {
                        throw e;
                    }
                }
            }

            // try again
            if (utf8mb4Unsupported) {
                try (Statement s = connection.createStatement()) {
                    for (String query : statements) {
                        s.addBatch(query.replace("utf8mb4", "utf8"));
                    }

                    s.executeBatch();
                }
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<User> loadUser(UUID uniqueId) {
        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_SELECT_BY_UID);
            try (PreparedStatement s = c.prepareStatement(query)) {
                s.setString(1, uniqueId.toString());
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {

                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    Date registeredAt = rs.getDate("registered_at");
                    String registeredIp = rs.getString("registered_ip");
                    String realname = rs.getString("realname");

                    User user = new User(uniqueId, realname, registeredIp, registeredAt);

                    Optional<UserPassword> password = loadUserPassword(uniqueId);
                    password.ifPresent(user::changePassword);

                    return Optional.of(user);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(String playerName) {
        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_SELECT_BY_NAME);
            try (PreparedStatement s = c.prepareStatement(query)) {
                s.setString(1, playerName.toLowerCase());
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {

                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    UUID uniqueId = UUID.fromString(rs.getString("unique_id"));
                    String registeredIp = rs.getString("registered_ip");
                    Date registeredAt = rs.getDate("registered_at");
                    String realname = rs.getString("realname");

                    User user = new User(uniqueId, realname, registeredIp, registeredAt);

                    Optional<UserPassword> password = loadUserPassword(uniqueId);
                    password.ifPresent(user::changePassword);

                    return Optional.of(user);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    private Optional<UserPassword> loadUserPassword(UUID uniqueId) {
        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_PASSWORD_SELECT);
            try (PreparedStatement s = c.prepareStatement(query)) {
                s.setString(1, uniqueId.toString());
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {

                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    HashMethodType hashmethodType = HashMethodType.valueOf(
                        rs.getString("hash_method_type"));

                    String passwordString = rs.getString("password");
                    Date createdAt = rs.getDate("created_at");
                    Date updatedAt = rs.getDate("updated_at");

                    UserPassword password = new UserPassword(
                        uniqueId,
                        passwordString,
                        hashmethodType,
                        createdAt,
                        updatedAt
                    );

                    return Optional.of(password);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    public void changeUserPassword(@NonNull UserPassword password) {
        password.getIOLock().lock();
        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_PASSWORD_SELECT_ID);
            try (PreparedStatement s = c.prepareStatement(query)) {
                s.setString(1, password.uniqueId.toString());
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {
                    String resultQuery = !rs.next()
                        ? USER_PASSWORD_INSERT
                        : USER_PASSWORD_UPDATE;

                    saveUserPassword(c, resultQuery, password);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            password.getIOLock().unlock();
        }
    }

    private void saveUserPassword(@NonNull Connection c, @NonNull String query, @NonNull UserPassword password) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(this.statementProcessor.apply(query))) {
            s.setString(1, password.uniqueId.toString());
            s.setString(2, password.password);
            s.setString(3, password.hashMethodType.toString());
            s.executeUpdate();
        }
    }

    @Override
    public void saveUser(@NonNull User user) {
        user.getOILock().lock();
        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_SELECT_ID_BY_UUID);
            try (PreparedStatement s = c.prepareStatement(this.statementProcessor.apply(query))) {
                s.setString(1, user.uniqueId.toString());
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) {
                        saveUser(c, user, USER_INSERT);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            user.getOILock().unlock();
        }
    }

    public void saveUser(Connection c, User user, String query) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(this.statementProcessor.apply(query))) {
            s.setString(1, user.uniqueId.toString());
            s.setString(2, user.username);
            s.setString(3, user.realname);
            s.setString(4, user.registeredIp);
            s.executeUpdate();
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() {
        Set<UUID> uuids = new HashSet<>();

        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_SELECT_ALL_IDS);
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery(this.statementProcessor.apply(query))) {
                    while (rs.next()) {
                        uuids.add(UUID.fromString(rs.getString("unique_id")));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return uuids;
    }

    @Override
    public UUID getPlayerUniqueId(String username) {
        username = username.toLowerCase();

        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_SELECT_ID_BY_USERNAME);
            try (PreparedStatement s = c.prepareStatement(this.statementProcessor.apply(query))) {
                s.setString(1, username);
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    return UUID.fromString(rs.getString("unique_id"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public String getPlayerName(UUID uniqueId) {
        try (Connection c = this.connectionFactory.getConnection()) {
            String query = this.statementProcessor.apply(USER_SELECT_USERNAME_BY_ID);
            try (PreparedStatement s = c.prepareStatement(this.statementProcessor.apply(query))) {
                s.setString(1, uniqueId.toString());
                s.setMaxRows(1);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    return rs.getString("username");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
