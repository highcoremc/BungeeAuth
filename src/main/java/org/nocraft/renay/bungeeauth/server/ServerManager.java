package org.nocraft.renay.bungeeauth.server;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.scheduler.Scheduler;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldMayBeFinal")
public class ServerManager {

    private final Map<ServerInfo, ServerType> servers = new HashMap<>();
    private final Map<ServerInfo, ServerType> actualServers = new ConcurrentHashMap<>();

    private final BungeeAuthPlugin plugin;

    public ServerManager(BungeeAuthPlugin plugin, Scheduler scheduler) {
        this.plugin = plugin;
        scheduler.asyncRepeating(this::actual, 500, TimeUnit.MILLISECONDS);
    }

    public synchronized void addServer(ServerType type, ServerInfo server) {
        this.servers.put(server, type);
    }

    private synchronized void actual() {
        this.servers.forEach((server, type) -> {
            try (Socket s = new Socket()) {
                s.connect(server.getSocketAddress(), 10);
                this.actualServers.put(server, type);
            } catch (IOException e) {
                this.actualServers.remove(server);
            }
        });
    }

    public void connect(ServerType type, ProxiedPlayer p) {
        if (this.actualServers.size() == 0 || !this.actualServers.containsValue(type)) {
            disconnect(p);
            return;
        }

        Map<ServerInfo, ServerType> filtered =
                filterActualByType(type);
        if (filtered.size() == 0) {
            disconnect(p);
            return;
        }

        try {
            p.connect(calculateRandomServer(filtered));
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            disconnect(p);
        }
    }

    private Map<ServerInfo, ServerType> filterActualByType(ServerType type) {
        HashMap<ServerInfo, ServerType> result = new HashMap<>();
        for (Map.Entry<ServerInfo, ServerType> entry : servers.entrySet()) {
            if (type.equals(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public void disconnect(ProxiedPlayer p) {
        Message message = plugin.getMessageConfig()
                .get(MessageKeys.NO_ACTUAL_SERVER);
        p.disconnect(message.asComponent());
    }

    private ServerInfo calculateRandomServer(Map<ServerInfo, ServerType> servers) throws IllegalStateException {
        Iterator<Map.Entry<ServerInfo, ServerType>> iterator =
                servers.entrySet().iterator();
        int index = new Random().nextInt(servers.size());

        for (int i = 0; i < servers.size(); i++) {
            if (!iterator.hasNext()) {
                break;
            }

            Map.Entry<ServerInfo, ServerType> entry = iterator.next();

            if (index == i) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException("Can not calculate random actual server.");
    }

    public ServerType getServerType(ServerInfo info) {
        ServerType type = this.servers.get(info);

        if (type == null) {
            return ServerType.UNKNOWN;
        }

        return type;
    }

    public ServerType getServerType(String serverName) {
        for (ServerInfo server : this.servers.keySet()) {
            if (serverName.equals(server.getName())) {
                return this.servers.get(server);
            }
        }

        return ServerType.UNKNOWN;
    }

    public ServerInfo getServer(ServerType type) throws IllegalStateException {
        return calculateRandomServer(filterActualByType(type));
    }
}
