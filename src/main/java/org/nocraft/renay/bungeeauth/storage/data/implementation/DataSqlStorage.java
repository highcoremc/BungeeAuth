package org.nocraft.renay.bungeeauth.storage.data.implementation;

import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.storage.data.DataStorage;
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
    private static final String USER_INSERT = "INSERT INTO '{prefix}users' (id, username, active_session) VALUES(:p1, :p2, :p3)";
    private static final String USER_SELECT_BY_ID = "SELECT username FROM '{prefix}users' WHERE unique_id=:p1";
    private static final String USER_SELECT_ALL_IDS = "SELECT id FROM '{prefix}users'";

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
            tableExists = tableExists(c, this.statementProcessor.apply("{prefix}user_permissions"));
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
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_BY_ID))) {
                User user = query.withParams(uniqueId).executeAndFetchFirst(User.class);

                return user == null ? Optional.empty() : Optional.of(user);
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public void saveUser(User user) throws SQLException {
//        user.getIoLock().lock();
        try (Connection c = this.connectionFactory.getConnection()) {

        } finally {
//            user.getIoLock().unlock();
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() {
        Set<UUID> uuids = new HashSet<>();

        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_ALL_IDS))) {
                List<User> rs = query.executeAndFetch(User.class);

                if (!rs.isEmpty()) {
                    rs.forEach(u -> uuids.add(u.getUniqueId()));
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
                return query.withParams(username)
                        .executeAndFetchFirst(User.class)
                        .getUniqueId();
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
                return query.withParams(uniqueId).executeAndFetchFirst(User.class).getName();
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
