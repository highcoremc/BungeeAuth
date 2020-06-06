package org.nocraft.renay.bungeeauth.storage;

import java.util.function.Function;

public interface ConnectionFactory <E extends AutoCloseable> {
    String getImplementationName();

    void init();

    void shutdown();

    E getConnection() throws Exception;

    Function<String, String> getStatementProcessor();
}