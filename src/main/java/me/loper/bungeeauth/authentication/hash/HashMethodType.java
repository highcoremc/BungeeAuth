package me.loper.bungeeauth.authentication.hash;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum HashMethodType {

    BCRYPT("BCRYPT", "bcrypt"),
    SHA256("SHA256", "sha256");

    private final String name;

    private final List<String> identifiers;

    HashMethodType(String name, String... identifiers) {
        this.name = name;
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static HashMethodType parse(String name, HashMethodType def) {
        for (HashMethodType t : values()) {
            for (String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        return def;
    }

    public static HashMethodType parse(String name) {
        for (HashMethodType t : values()) {
            for (String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        throw new IllegalStateException("Unable to parse hash method type.");
    }

    public String getName() {
        return this.name;
    }

    public List<String> getIdentifiers() {
        return this.identifiers;
    }
}
