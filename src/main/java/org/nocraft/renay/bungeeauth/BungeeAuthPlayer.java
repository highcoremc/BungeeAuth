package org.nocraft.renay.bungeeauth;

import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.session.Session;

import java.util.Date;
import java.util.Objects;

public class BungeeAuthPlayer {

    public final Date joinedAt = new Date();
    public final User user;
    public Session session;

    private boolean authenticated = false;

    public BungeeAuthPlayer(User user) {
        this.user = user;
    }

    public void changeActiveSession(Session session) {
        this.session = session;
    }

    public void authenticated() {
        this.authenticated = true;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BungeeAuthPlayer)) {
            return false;
        }

        BungeeAuthPlayer that = (BungeeAuthPlayer) o;

        if (authenticated != that.authenticated ||
            !joinedAt.equals(that.joinedAt) ||
            !Objects.equals(user, that.user)
        ) {
            return false;
        }

        return Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        int result = joinedAt.hashCode();

        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (authenticated ? 1 : 0);

        return result;
    }
}
