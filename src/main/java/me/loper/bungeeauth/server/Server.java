package me.loper.bungeeauth.server;

import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;
import java.net.Socket;

public class Server {

    private ServerInfo target;
    private Status status;

    public Server(ServerInfo target) {
        if (null == target) {
            throw new IllegalArgumentException("target can not be null.");
        }

        reconnect(target);
    }

    public static Server wrap(ServerInfo serverInfo) {
        return new Server(serverInfo);
    }

    public Server reconnect() {
        reconnect(null);

        return this;
    }

    public Server reconnect(ServerInfo t) {
        if (null == t) {
            t = target;
        }

        this.status = ping(t)
            ? Status.SUCCESS
            : Status.FAILURE;

        target = t;

        return this;
    }

    public boolean ping(ServerInfo target) {
        if (null == target) {
            return false;
        }

        try (Socket s = new Socket()) {
            s.connect(target.getSocketAddress(), 10);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public Status getStatus() {
        return status;
    }

    public ServerInfo getTarget() {
        return this.target;
    }

    enum Status {
        SUCCESS("success"),
        FAILURE("failure");

        private final String value;

        Status(String value){
            this.value = value;
        }

        public boolean isSuccess() {
            return value.equals(SUCCESS.getValue());
        }

        private String getValue() {
            return this.value;
        }

        public boolean isFailure() {
            return this.value.equals(FAILURE.getValue());
        }
    }
}
