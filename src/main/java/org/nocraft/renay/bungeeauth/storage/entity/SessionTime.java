package org.nocraft.renay.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Date;

public class SessionTime implements Serializable {
    private static final long serialVersionUID = -5511720394017086753L;

    public final Date startTime;
    public final Date endTime;
    public Date closedTime;

    public SessionTime(@NonNull Date startTime, @NonNull Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SessionTime(@NonNull Date endTime) {
        this.startTime = new Date();
        this.endTime = endTime;
    }

    public void closedAt(Date closed) {
        this.closedTime = closed;
    }
}
