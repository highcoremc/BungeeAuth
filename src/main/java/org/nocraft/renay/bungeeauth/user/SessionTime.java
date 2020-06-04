package org.nocraft.renay.bungeeauth.user;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;

public class SessionTime {

    public final Date startTime;
    public final Date endTime;
    public Date closedTime;

    public SessionTime(@NonNull Date startTime, @NonNull Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void closedAt(Date closed) {
        this.closedTime = closed;
    }
}
