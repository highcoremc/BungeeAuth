package org.nocraft.renay.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class User {

    public final UUID uniqueId;
    public final String username;
    public final String realname;
    private UserPassword password;

    /**
     * Lock used by Storage implementations to prevent concurrent read/writes
     * @see #getOILock()
     */
    private Lock ioLock = new ReentrantLock();

    public final String registeredIp;
    private Date registeredAt;

    public User(@NonNull UUID uniqueId, @NonNull String username, @NonNull String registeredIp) {
        this.uniqueId = uniqueId;
        this.username = username.toLowerCase();
        this.registeredIp = registeredIp;
        this.realname = username;
    }

    public boolean isRegistered() {
        return null != this.registeredAt;
    }

    public void changePassword(UserPassword password) {
        this.password = password;
    }

    public UserPassword getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return null != password;
    }

    public String toString() {
        String passwd = null == password ? null : password.toString();

        return String.format("%s, %s, %s, %s, %s",
                uniqueId.toString(), username,
                realname, registeredIp, passwd
        );
    }

    public Lock getOILock() {
        if (null == this.ioLock) {
            this.ioLock = new ReentrantLock();
        }

        return this.ioLock;
    }
}
