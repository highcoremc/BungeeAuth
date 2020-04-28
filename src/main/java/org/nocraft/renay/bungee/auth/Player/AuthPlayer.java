package org.nocraft.renay.bungee.auth.Player;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Pattern;

public class AuthPlayer {

    @Getter
    private final ProxiedPlayer player;

    @Getter
    private final String name;

    @Getter
    private String lastIp;

    private String passwordHash;

    @Getter
    private long session;

    private boolean authenticated;

    public AuthPlayer(final ProxiedPlayer p) {
        authenticated = false;
        name = p.getName();
        player = p;
    }

    public void fillFromDatabase(String passwordHash, String lastIp, long session) {
        this.passwordHash = passwordHash;
        this.session = session;
        this.lastIp = lastIp;
    }

    public static AuthPlayer create(ProxiedPlayer p, String hash, String lastIp, long session) {
        AuthPlayer self = new AuthPlayer(p);
        self.fillFromDatabase(hash, lastIp, session);
        return self;
    }

    public static void validate(ProxiedPlayer p) {
        if (!Pattern.matches("/^[A-Za-z0-9_]+$/", p.getName())) {
            throw new InvalidNicknameException();
        }
    }

    public boolean isRegistered() {
        return (passwordHash != null);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public AuthPlayer authenticated() {
        this.authenticated = true;
        return this;
    }
}
