package org.nocraft.renay.bungeeauth.storage.data.implementation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.data.DataStorage;
import org.nocraft.renay.bungeeauth.storage.entity.UserPassword;
import org.nocraft.renay.bungeeauth.storage.implementation.sql.SchemaReader;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.implementation.sql.connection.SqlConnectionFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2oException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataSqlStorage implements DataStorage {

    private static final String USER_SELECT_ID_BY_USERNAME = "SELECT id FROM '{prefix}players' WHERE username=:p1 LIMIT 1";
    private static final String USER_SELECT_USERNAME_BY_ID = "SELECT username FROM '{prefix}players' WHERE unique_id=:p1 LIMIT 1";
    private static final String USER_INSERT = "INSERT INTO '{prefix}users' (unique_id, username, realname, registered_ip) VALUES(:p1, :p2, :p3, :p4)";
    private static final String USER_UPDATE = "UPDATE '{prefix}users' SET username = :p2, realname = :p3, registered_ip = :p4 WHERE unique_id = :p1";
    private static final String USER_SELECT_BY_UID = "SELECT unique_id,username,realname,registered_at,registered_ip FROM '{prefix}users' WHERE unique_id=:p1";
    private static final String USER_SELECT_ID_BY_UID = "SELECT id FROM '{prefix}users' WHERE unique_id=:p1";
    private static final String USER_SELECT_ALL_IDS = "SELECT id FROM '{prefix}users'";

    private static final String USER_PASSWORD_SELECT_ID = "SELECT id FROM '{prefix}user_password' WHERE unique_id = :p1";
    private static final String USER_PASSWORD_SELECT = "SELECT unique_id,password,hash_method_type,updated_at,created_at FROM '{prefix}user_password' WHERE unique_id = :p1";
    private static final String USER_PASSWORD_INSERT = "INSERT INTO '{prefix}user_password' (unique_id, password, hash_method_type) VALUES (:p1, :p2, :p3)";
    private static final String USER_PASSWORD_UPDATE = "UPDATE '{prefix}user_password' SET password = :p2, hash_method_type = :p3 WHERE unique_id = :p1";

    private final BungeeAuthPlugin plugin;

    private final SqlConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public DataSqlStorage(BungeeAuthPlugin plugin, SqlConnectionFactory connectionFactory, String tablePrefix) {
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
        try (Connection c = this.connectionFactory.getConnection()) {
            tableExists = tableExists(c, this.statementProcessor.apply("{prefix}users"));
        }

        if (!tableExists) {
            applySchema();
        }
    }

    private boolean tableExists(Connection connection, String table) throws SQLException {
        try (ResultSet rs = connection.getJdbcConnection().getMetaData().getTables(null, null, "%", null)) {
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

        String schemaFileName = "org/nocraft/renay/bungeeauth/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";
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

            try (Statement s = connection.getJdbcConnection().createStatement()) {
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
                try (Statement s = connection.getJdbcConnection().createStatement()) {
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
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_BY_UID))) {
                User user = query
                        .withParams(uniqueId.toString())
                        .executeAndFetchFirst(User.class);

                if (null == user) {
                    return Optional.empty();
                }

                Optional<UserPassword> password = loadUserPassword(uniqueId);
                password.ifPresent(user::changePassword);

                return Optional.of(user);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(String playerName) throws Exception {
        return Optional.empty();
    }

    private Optional<UserPassword> loadUserPassword(UUID uniqueId) {
        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_PASSWORD_SELECT))) {
                UserPassword password = query
                        .withParams(uniqueId.toString())
                        .executeAndFetchFirst(UserPassword.class);

                return Optional.ofNullable(password);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    public void changeUserPassword(@NonNull UserPassword password) {
        password.getIOLock().lock();
        try (Connection c = this.connectionFactory.getConnection()) {
            String apply = this.statementProcessor
                .apply(USER_PASSWORD_SELECT_ID);

            try (Query query = c.createQuery(apply)) {
                UserPassword result = query
                        .withParams(password.uniqueId.toString())
                        .executeAndFetchFirst(UserPassword.class);

                String queryString = null == result
                        ? USER_PASSWORD_INSERT
                        : USER_PASSWORD_UPDATE;

                saveUserPassword(c, queryString, password);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            password.getIOLock().unlock();
        }
    }

    private void saveUserPassword(@NonNull Connection c, @NonNull String queryString, @NonNull UserPassword password) {
        try (Query q = c.createQuery(this.statementProcessor.apply(queryString))) {
            q.addParameter("p1", password.uniqueId.toString());
            q.addParameter("p2", password.password);
            q.addParameter("p3", password.hashMethodType);
            q.executeUpdate();
        }
    }

    @Override
    public void saveUser(@NonNull User user) {
        user.getOILock().lock();
        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_ID_BY_UID))) {
                User result = query
                        .withParams(user.uniqueId.toString())
                        .executeAndFetchFirst(User.class);
                if (result == null) {
                    saveUser(c, user, USER_INSERT);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            user.getOILock().unlock();
        }
    }

    public void saveUser(Connection c, User user, String q) {
        try (Query query = c.createQuery(this.statementProcessor.apply(q))) {
            query.addParameter("p1", user.uniqueId.toString());
            query.addParameter("p2", user.username);
            query.addParameter("p3", user.realname);
            query.addParameter("p4", user.registeredIp);
            query.executeUpdate();
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() {
        Set<UUID> uuids = new HashSet<>();

        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_ALL_IDS))) {
                List<User> rs = query.executeAndFetch(User.class);

                if (!rs.isEmpty()) {
                    rs.forEach(u -> uuids.add(u.uniqueId));
                }
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return uuids;
    }

    @Override
    public UUID getPlayerUniqueId(String username) {
        username = username.toLowerCase();

        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_ID_BY_USERNAME))) {
                return query.withParams(username).executeAndFetchFirst(User.class).uniqueId;
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public String getPlayerName(UUID uniqueId) {
        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_USERNAME_BY_ID))) {
                return query.withParams(uniqueId).executeAndFetchFirst(User.class).username;
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
