package org.nocraft.renay.bungeeauth;

import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.session.Session;

import java.util.Date;

public class BungeeAuthPlayer {

    public final Date joinedAt = new Date();
    public final User user;
    public Session session;

    public BungeeAuthPlayer(User user) {
        this.user = user;
    }

    public void changeActiveSession(Session session) {
        this.session = session;
    }
}
