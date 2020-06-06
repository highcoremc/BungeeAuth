package org.nocraft.renay.bungeeauth;

import net.md_5.bungee.connection.InitialHandler;
import org.nocraft.renay.bungeeauth.storage.entity.SessionTime;
import org.nocraft.renay.bungeeauth.storage.session.Session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.Date;

public class SessionFactory {

    private final int timeout;

    public SessionFactory(int timeout) {
        if (timeout < 300) {
            timeout = 300;
        }

        this.timeout = timeout;
    }

    public Session create(InitialHandler c) {
        String address = this.extractHostString(c.getSocketAddress());
        Date endTime = this.createEndTime(timeout);

        SessionTime time = new SessionTime(endTime);

        return new Session(c.getName(), c.getUniqueId(), time, address);
    }

    private String extractHostString(SocketAddress address) {
        return ((InetSocketAddress) address).getHostString();
    }

    private Date createEndTime(int timeout) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, timeout);

        return now.getTime();
    }
}
