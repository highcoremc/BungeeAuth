package org.nocraft.renay.bungeeauth.storage;

import org.nocraft.renay.bungeeauth.BungeeAuth;

import java.util.function.Function;

public interface ConnectionFactory <E> {
    String getImplementationName();

    void init();

    void shutdown();

    E getConnection() throws Exception;

    Function<String, String> getStatementProcessor();
}