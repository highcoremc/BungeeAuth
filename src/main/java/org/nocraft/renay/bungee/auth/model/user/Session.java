package org.nocraft.renay.bungee.auth.model.user;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;
import java.util.UUID;

public class Session {

    public final UUID id;
    public final User user;
    public final String ipAddress;
    public final SessionTime time;

    public Session(@NonNull User user, @NonNull SessionTime sessionTime, String address) {
        this.id = UUID.fromString(user.userName() + sessionTime.startTime.toString());
        this.user = user;
        this.time = sessionTime;
        this.ipAddress = address;
    }

    public void close(Date time) {
        this.time.closedAt(time);
    }
}
