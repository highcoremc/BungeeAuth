package org.nocraft.renay.bungeeauth.storage.session;

import com.google.common.collect.ImmutableList;

public enum SessionStorageType {

    // Storage for cache
    REDIS("Redis", "redis");

    private final String name;

    private final ImmutableList<String> identifiers;

    SessionStorageType(String name, String... identifiers) {
        this.name = name;
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static SessionStorageType parse(String name, SessionStorageType def) {
        for (SessionStorageType t : values()) {
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

    public ImmutableList<String> getIdentifiers() {
        return identifiers;
    }
}
