package me.loper.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionTime)) return false;

        SessionTime that = (SessionTime) o;

        if (!Objects.equals(startTime, that.startTime)) return false;
        if (!Objects.equals(endTime, that.endTime)) return false;

        return Objects.equals(closedTime, that.closedTime);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();

        result = 31 * result + endTime.hashCode();
        result = 31 * result + (closedTime != null ? closedTime.hashCode() : 0);

        return result;
    }
}
