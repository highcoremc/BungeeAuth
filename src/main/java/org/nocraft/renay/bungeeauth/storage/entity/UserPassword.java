package org.nocraft.renay.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.hash.HashMethod;
import org.nocraft.renay.bungeeauth.hash.HashMethodType;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserPassword {

    public final UUID uniqueId;
    public final String password;

    public final HashMethodType methodType;

    private Date updatedAt;
    private Date createdAt;

    /**
     * Lock used by Storage implementations to prevent concurrent read/writes
     * @see #getIOLock()
     */
    private Lock ioLock = new ReentrantLock();

    public UserPassword(UUID uniqueId, String password, HashMethodType methodType) {
        this.methodType = methodType;
        this.uniqueId = uniqueId;
        this.password = password;
    }

    public boolean verify(@NonNull HashMethod method, @NonNull String entry) {
        return method.verify(this.password, entry);
    }

    public @NonNull Date getCreatedAt() {
        return this.createdAt;
    }

    public @NonNull Date getUpdatedAt() {
        return this.updatedAt;
    }

    public String toString() {
        return String.format("%s,%s,%s", this.methodType.toString(), this.uniqueId.toString(), this.password);
    }

    public Lock getIOLock() {
        if (null == this.ioLock) {
            this.ioLock = new ReentrantLock();
        }

        return this.ioLock;
    }
}
