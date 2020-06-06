package org.nocraft.renay.bungeeauth;

import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.session.Session;

public class BungeeAuthPlayer {
    public final User user;
    public Session session;

    public BungeeAuthPlayer(User user) {
        this.user = user;
    }

    public void changeActiveSession(Session session) {
        this.session = session;
    }
}
