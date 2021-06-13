package me.loper.bungeeauth.storage.entity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class SessionLifetime implements Serializable {
    private static final long serialVersionUID = -5511720394017086753L;

    public final Date startTime;
    public final Date endTime;
    public @Nullable Date closedTime;

    public SessionLifetime(@NonNull Date startTime, @NonNull Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SessionLifetime(@NonNull Date endTime) {
        this.startTime = new Date();
        this.endTime = endTime;
    }

    public void closedAt(Date closed) {
        this.closedTime = closed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SessionLifetime)) {
            return false;
        }

        SessionLifetime that = (SessionLifetime) o;

        return Objects.equals(endTime, that.endTime)
            && Objects.equals(startTime, that.startTime)
            && Objects.equals(closedTime, that.closedTime);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();

        result = 31 * result + endTime.hashCode();
        result = 31 * result + (closedTime != null ? closedTime.hashCode() : 0);

        return result;
    }

    public boolean isClosed() {
        return null != this.closedTime && this.closedTime.after(new Date());
    }
}
