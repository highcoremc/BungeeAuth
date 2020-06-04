package org.nocraft.renay.bungeeauth.storage.implementation.sql.connection;

import org.nocraft.renay.bungeeauth.storage.ConnectionFactory;
import org.sql2o.Connection;

import java.sql.SQLException;

public interface SqlConnectionFactory extends ConnectionFactory<Connection> {
    @Override
    Connection getConnection() throws SQLException;
}
