package org.nocraft.renay.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.hash.HashMethod;

import java.util.UUID;

public class UserPassword {

    public final UUID uniqueId;
    public final String password;

    public final long createdAt;
    public long updatedAt;

    public UserPassword(User user, String password) {
        this.password = password;
        this.uniqueId = user.getUniqueId();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean verify(@NonNull HashMethod method, @NonNull String entry) {
        return method.verify(this.password, entry);
    }
}
