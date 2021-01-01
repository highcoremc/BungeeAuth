package org.nocraft.renay.bungeeauth.storage.data;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum DataStorageType {

    // Remote databases
    POSTGRESQL("PostgreSQL", "postgresql"),
    REDIS("Redis", "redis");

    private final String name;

    private final List<String> identifiers;

    DataStorageType(String name, String... identifiers) {
        this.name = name;
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static DataStorageType parse(String name, DataStorageType def) {
        for (DataStorageType t : values()) {
            for (String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        return def;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getIdentifiers() {
        return this.identifiers;
    }
}
