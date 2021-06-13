package me.loper.bungeeauth.storage.entity;

import me.loper.bungeeauth.authentication.hash.HashMethod;
import me.loper.bungeeauth.authentication.hash.HashMethodType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserPassword {

    public final UUID uniqueId;
    public final String password;

    public final HashMethodType hashMethodType;

    public final Date updatedAt;
    public final Date createdAt;

    /**
     * Lock used by Storage implementations to prevent concurrent read/writes
     *
     * @see #getIOLock()
     */
    private Lock ioLock = new ReentrantLock();

    public UserPassword(UUID uniqueId, String password, HashMethodType type) {
        this.hashMethodType = type;
        this.uniqueId = uniqueId;
        this.password = password;
        this.updatedAt = new Date();
        this.createdAt = new Date();
    }

    public UserPassword(UUID uniqueId, String password, HashMethodType type, Date createdAt, Date updatedAt) {
        this.hashMethodType = type;
        this.uniqueId = uniqueId;
        this.password = password;
        this.updatedAt = createdAt;
        this.createdAt = updatedAt;
    }

    public boolean verify(@NonNull HashMethod method, @NonNull String entry) {
        return method.verify(entry, this.password);
    }

    public String toString() {
        return String.format("%s,%s,%s", this.hashMethodType.toString(), this.uniqueId.toString(), this.password);
    }

    public Lock getIOLock() {
        if (null == this.ioLock) {
            this.ioLock = new ReentrantLock();
        }

        return this.ioLock;
    }
}
