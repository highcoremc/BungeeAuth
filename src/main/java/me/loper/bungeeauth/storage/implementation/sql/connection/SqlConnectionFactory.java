package me.loper.bungeeauth.storage.implementation.sql.connection;

import me.loper.bungeeauth.storage.ConnectionFactory;
import org.sql2o.Connection;

import java.sql.SQLException;

public interface SqlConnectionFactory extends ConnectionFactory<Connection> {
    @Override
    Connection getConnection() throws SQLException;
}
