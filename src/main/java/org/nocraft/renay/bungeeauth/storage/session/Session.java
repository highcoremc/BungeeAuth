package org.nocraft.renay.bungeeauth.storage.session;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.entity.SessionTime;

import java.util.Date;
import java.util.UUID;

public class Session {

    public final String id;
    public final @NonNull UUID userId;
    public final String ipAddress;
    public final SessionTime time;

    public Session(@NonNull String userName, @NonNull UUID uniqueId, @NonNull SessionTime sessionTime, String address) {
        this.ipAddress = address;
        this.time = sessionTime;
        this.userId = uniqueId;
        this.id = userName;
    }

    public Session(@NonNull User user, @NonNull SessionTime sessionTime, String address) {
        this.id = user.getName() + sessionTime.startTime.toString();
        this.userId = user.getUniqueId();
        this.ipAddress = address;
        this.time = sessionTime;
    }

    public void close(Date time) {
        this.time.closedAt(time);
    }
}
