package me.loper.bungeeauth.storage.session;

import org.checkerframework.checker.nullness.qual.NonNull;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.SessionLifetime;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Session implements Serializable {
    private static final long serialVersionUID = 49315071413734952L;

    public final String username;
    public final @NonNull UUID userId;
    public final String ipAddress;
    public final SessionLifetime lifeTime;

    private ReentrantLock ioLock = new ReentrantLock();

    public Session(@NonNull String userName, @NonNull UUID uniqueId, @NonNull SessionLifetime lifeTime, String address) {
        this.ipAddress = address;
        this.lifeTime = lifeTime;
        this.userId = uniqueId;
        this.username = userName;
    }

    public Session(@NonNull User user, @NonNull SessionLifetime sessionTime, String address) {
        this.username = user.username + sessionTime.startTime.toString();
        this.userId = user.uniqueId;
        this.ipAddress = address;
        this.lifeTime = sessionTime;
    }

    public Session(Session oldSession, SessionLifetime time) {
        this.username = oldSession.username;
        this.userId = oldSession.userId;
        this.ipAddress = oldSession.ipAddress;
        this.lifeTime = time;
    }

    public void close(Date time) {
        this.lifeTime.closedAt(time);
    }

    public boolean isActive() {
        return this.lifeTime.endTime.after(new Date())
            && !this.lifeTime.isClosed();
    }

    public Lock getIOLock() {
        if (this.ioLock == null) {
            this.ioLock = new ReentrantLock();
        }
        return this.ioLock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Session)) {
            return false;
        }

        Session session = (Session) o;

        return Objects.equals(ipAddress, session.ipAddress)
            && Objects.equals(username, session.username)
            && Objects.equals(ioLock, session.ioLock)
            && Objects.equals(lifeTime, session.lifeTime)
            && userId.equals(session.userId)
        ;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;

        result = 31 * result + userId.hashCode();
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        result = 31 * result + (lifeTime != null ? lifeTime.hashCode() : 0);
        result = 31 * result + (ioLock != null ? ioLock.hashCode() : 0);

        return result;
    }
}
