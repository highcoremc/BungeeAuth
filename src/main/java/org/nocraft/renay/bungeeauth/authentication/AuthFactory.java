package org.nocraft.renay.bungeeauth.authentication;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethod;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodFactory;
import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodType;
import org.nocraft.renay.bungeeauth.storage.entity.SessionTime;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.entity.UserPassword;
import org.nocraft.renay.bungeeauth.storage.session.Session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class AuthFactory {

    private final HashMethodType hashMethodType;
    private final BungeeAuthPlugin plugin;
    private final int sessionTimeout;

    public AuthFactory(BungeeAuthPlugin plugin, Integer sessionTimeout, HashMethodType hashMethodType) {
        this.sessionTimeout = sessionTimeout;
        this.hashMethodType = hashMethodType;
        this.plugin = plugin;
    }

    public Optional<Session> createSession(UUID uniqueId) {
        ProxiedPlayer p = this.plugin.getProxy().getPlayer(uniqueId);

        if (null == p) {
            return Optional.empty();
        }

        return Optional.of(createSession(p.getPendingConnection()));
    }

    public Session createSession(PendingConnection c) {
        String address = this.extractHostString(c.getSocketAddress());
        Date endTime = this.createEndTime(this.sessionTimeout);
        SessionTime time = new SessionTime(endTime);

        return new Session(c.getName(), c.getUniqueId(), time, address);
    }

    public String extractHostString(SocketAddress address) {
        return ((InetSocketAddress) address).getHostString();
    }

    public Date createEndTime(int timeout) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, timeout);

        return now.getTime();
    }

    public UserPassword createUserPassword(ProxiedPlayer player, String rawPassword) {
        HashMethod method = HashMethodFactory.create(this.hashMethodType);
        String hash = method.hash(rawPassword);
        UUID uniqueId = player.getUniqueId();

        return new UserPassword(uniqueId, hash, this.hashMethodType);
    }

    public UserPassword createUserPassword(User user, String rawPassword) {
        HashMethod method = HashMethodFactory.create(this.hashMethodType);
        String hash = method.hash(rawPassword);
        UUID uniqueId = user.uniqueId;

        return new UserPassword(uniqueId, hash, this.hashMethodType);
    }
}
