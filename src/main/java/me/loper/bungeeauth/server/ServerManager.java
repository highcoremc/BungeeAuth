package me.loper.bungeeauth.server;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.BungeeSchedulerAdapter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldMayBeFinal")
public class ServerManager {

    private final Map<Server, ServerType> servers = new HashMap<>();
    private volatile Map<Server, ServerType> actualServers = new HashMap<>();
    private Map<ServerInfo, Server> serverInfo = new HashMap<>();

    private final BungeeAuthPlugin plugin;

    public ServerManager(BungeeAuthPlugin plugin, BungeeSchedulerAdapter scheduler) {
        this.plugin = plugin;

        scheduler.asyncRepeating(this::actualServerList, 700, TimeUnit.MILLISECONDS);
    }

    public synchronized void addServer(ServerType type, Server server) {
        this.servers.put(server, type);
        this.serverInfo.put(server.getTarget(), server);
    }

    private synchronized void actualServerList() {
        this.servers.forEach((server, type) -> {
            if (server.getStatus().isFailure()) {
                server.reconnect(server.getTarget());
            }

            if (server.getStatus().isSuccess()) {
                this.actualServers.put(server, type);
                return;
            }

            this.actualServers.remove(server);
        });
    }

    public void connect(ServerType type, ProxiedPlayer p) {
        if (this.actualServers.size() == 0 || !this.actualServers.containsValue(type)) {
            disconnect(p);
            return;
        }

        Map<Server, ServerType> filtered =
                filterActualByType(type);
        if (filtered.size() == 0) {
            disconnect(p);
            return;
        }

        try {
            p.connect(getRandomServer(filtered).getTarget());
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            disconnect(p);
        }
    }

    private Map<Server, ServerType> filterActualByType(ServerType type) {
        HashMap<Server, ServerType> result = new HashMap<>();
        for (Map.Entry<Server, ServerType> entry : servers.entrySet()) {
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

    private Server getRandomServer(Map<Server, ServerType> servers) throws IllegalStateException {
        Iterator<Map.Entry<Server, ServerType>> iterator =
                servers.entrySet().iterator();
        int index = new Random().nextInt(servers.size());

        for (int i = 0; i < servers.size(); i++) {
            if (!iterator.hasNext()) {
                break;
            }

            Map.Entry<Server, ServerType> entry = iterator.next();

            if (index == i) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException("Can not calculate random actual server.");
    }

    public ServerType getServerTypeByServerInfo(ServerInfo info) {
        ServerType type =  this.servers.get(this.serverInfo.get(info));

        if (type == null) {
            return ServerType.UNKNOWN;
        }

        return type;
    }

    public Server getServer(ServerType type) throws IllegalStateException {
        return getRandomServer(filterActualByType(type));
    }
}
