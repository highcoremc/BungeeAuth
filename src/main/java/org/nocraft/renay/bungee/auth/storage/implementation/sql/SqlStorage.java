package org.nocraft.renay.bungee.auth.storage.implementation.sql;

import com.google.gson.reflect.TypeToken;
import org.nocraft.renay.bungee.auth.BungeeAuth;
import org.nocraft.renay.bungee.auth.model.user.User;
import org.nocraft.renay.bungee.auth.storage.implementation.StorageImplementation;
import org.nocraft.renay.bungee.auth.storage.implementation.sql.connection.ConnectionFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2oException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlStorage implements StorageImplementation {
    private static final Type LIST_STRING_TYPE = new TypeToken<List<String>>(){}.getType();

    private static final String USER_SELECT_ID_BY_USERNAME = "SELECT id FROM '{prefix}players' WHERE username=? LIMIT 1";
    private static final String USER_SELECT_USERNAME_BY_ID = "SELECT username FROM '{prefix}players' WHERE id=? LIMIT 1";
    private static final String PLAYER_INSERT = "INSERT INTO '{prefix}users' (id, username, active_session) VALUES(?, ?, ?)";
    private static final String USER_SELECT_BY_ID = "SELECT username, primary_group FROM '{prefix}players' WHERE id=?";
    private static final String USER_SELECT_ALL_IDS = "SELECT id FROM '{prefix}users'";

    private final BungeeAuth plugin;
    
    private final ConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public SqlStorage(BungeeAuth plugin, ConnectionFactory connectionFactory, String tablePrefix) {
        this.plugin = plugin;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public BungeeAuth getPlugin() {
        return this.plugin;
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

    private boolean tableExists(Connection c, String apply) {
        return false;
    }

    private void applySchema() throws IOException, SQLException {
        List<String> statements;

        String schemaFileName = "org/nocraft/renay/bungee/auth/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";
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
    public User loadUser(UUID uniqueId) {
        try (Connection c = this.connectionFactory.getConnection()) {
            try (Query query = c.createQuery(this.statementProcessor.apply(USER_SELECT_BY_ID))) {
                return query.withParams(uniqueId).executeAndFetchFirst(User.class);
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return null;
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
                    rs.forEach(u -> uuids.add(u.userId()));
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
                        .userId();
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
                return query.withParams(uniqueId).executeAndFetchFirst(User.class).userName();
            }
        } catch (SQLException | Sql2oException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
