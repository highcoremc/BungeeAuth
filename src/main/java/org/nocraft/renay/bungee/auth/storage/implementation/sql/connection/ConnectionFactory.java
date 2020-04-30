package org.nocraft.renay.bungee.auth.storage.implementation.sql.connection;

import org.sql2o.Connection;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public interface ConnectionFactory {

    String getImplementationName();

    void init();

    void shutdown() throws Exception;

    default Map<String, String> getMeta() {
        return Collections.emptyMap();
    }

    Function<String, String> getStatementProcessor();

    Connection getConnection() throws SQLException;

}
