package me.loper.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;
import java.util.Objects;
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
    public final Date registeredAt;

    public User(@NonNull UUID uniqueId, @NonNull String username, @NonNull String registeredIp) {
        this.uniqueId = uniqueId;
        this.username = username.toLowerCase();
        this.registeredIp = registeredIp;
        this.realname = username;
        this.registeredAt = new Date();
    }

    public boolean isRegistered() {
        return null != this.password;
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
                realname, registeredIp, passwd);
    }

    public Lock getOILock() {
        if (null == this.ioLock) {
            this.ioLock = new ReentrantLock();
        }

        return this.ioLock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;
        if (!Objects.equals(registeredIp, user.registeredIp) ||
            !Objects.equals(username, user.username) ||
            !Objects.equals(realname, user.realname) ||
            !uniqueId.equals(user.uniqueId) ||
            !password.equals(user.password) ||
            !ioLock.equals(user.ioLock)) {
            return false;
        }

        return registeredAt.equals(user.registeredAt);
    }

    @Override
    public int hashCode() {
        int result = uniqueId.hashCode();

        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (realname != null ? realname.hashCode() : 0);
        result = 31 * result + password.hashCode();
        result = 31 * result + ioLock.hashCode();
        result = 31 * result + registeredIp.hashCode();
        result = 31 * result + registeredAt.hashCode();

        return result;
    }
}
