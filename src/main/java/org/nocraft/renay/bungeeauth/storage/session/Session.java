package org.nocraft.renay.bungeeauth.storage.session;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.entity.SessionTime;

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
    public final SessionTime time;

    private ReentrantLock ioLock = new ReentrantLock();

    public Session(@NonNull String userName, @NonNull UUID uniqueId, @NonNull SessionTime sessionTime, String address) {
        this.ipAddress = address;
        this.time = sessionTime;
        this.userId = uniqueId;
        this.username = userName;
    }

    public Session(@NonNull User user, @NonNull SessionTime sessionTime, String address) {
        this.username = user.username + sessionTime.startTime.toString();
        this.userId = user.uniqueId;
        this.ipAddress = address;
        this.time = sessionTime;
    }

    public Session(Session oldSession, SessionTime time) {
        this.username = oldSession.username;
        this.userId = oldSession.userId;
        this.ipAddress = oldSession.ipAddress;
        this.time = time;
    }

    public void close(Date time) {
        this.time.closedAt(time);
    }

    public boolean isActive() {
        return this.time.endTime.after(new Date());
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

        if (!Objects.equals(username, session.username)) {
            return false;
        }

        if (!userId.equals(session.userId)) {
            return false;
        }

        if (!Objects.equals(ipAddress, session.ipAddress)) {
            return false;
        }

        if (!Objects.equals(time, session.time)) {
            return false;
        }

        return Objects.equals(ioLock, session.ioLock);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;

        result = 31 * result + userId.hashCode();
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (ioLock != null ? ioLock.hashCode() : 0);

        return result;
    }
}
