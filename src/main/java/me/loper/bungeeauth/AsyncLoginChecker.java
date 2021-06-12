package me.loper.bungeeauth;

import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.exception.AuthenticationException;
import me.loper.bungeeauth.server.ServerManager;
import me.loper.bungeeauth.server.ServerType;
import me.loper.bungeeauth.util.TitleBarApi;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncLoginChecker implements Runnable {

    private final Queue<ServerInfo> servers;
    private final BungeeAuthPlugin plugin;
    private final long maxAuthTime;

    /**
     * Checks async players on auth servers.
     *
     * @param servers List<String> Login servers
     */
    public AsyncLoginChecker(BungeeAuthPlugin plugin, List<String> servers) {
        this.plugin = plugin;
        this.servers = new ArrayBlockingQueue<>(servers.size());
        this.maxAuthTime = plugin.getConfiguration().get(ConfigKeys.MAX_AUTH_TIME);
        for (String server : servers) {
            ServerInfo info = plugin.getProxy()
                    .getServerInfo(server);
            if (null == info) {
                continue;
            }
            this.servers.add(info);
        }
    }

    @Override
    public synchronized void run() {
        TitleBarApi titleBarApi = new TitleBarApi(0, 120, 0);
        long now = System.currentTimeMillis();

        for (ServerInfo server : this.servers) {
            for (ProxiedPlayer player : server.getPlayers()) {
                BungeeAuthPlayer bungeeAuthPlayer = this.plugin
                    .getAuthPlayer(player.getUniqueId());

                if (bungeeAuthPlayer == null) {
                    tryAuthenticate(player);
                    return;
                }

                long timeLeft = (now - bungeeAuthPlayer.joinedAt.getTime()) / 1000;

                if (timeLeft >= this.maxAuthTime) {
                    Message message = plugin.getMessageConfig()
                        .get(MessageKeys.LOGIN_TIMEOUT);
                    player.disconnect(message.asComponent());
                    continue;
                }

                if (bungeeAuthPlayer.isAuthenticated()) {
                    ServerManager serverManager = this.plugin.getServerManager();
                    ServerType serverType = serverManager.getServerTypeByServerInfo(
                            player.getServer().getInfo());
                    if (serverType.equals(ServerType.LOGIN)) {
                        serverManager.connect(ServerType.GAME, player);
                    }
                    return;
                }

                if (bungeeAuthPlayer.user.isRegistered()) {
                    Message message = plugin.getMessageConfig()
                        .get(MessageKeys.LOGIN_CHAT_MESSAGE);
                    player.sendMessage(message.asComponent());
                    sendLoginTitle(titleBarApi, player);
                } else {
                    Message message = plugin.getMessageConfig()
                        .get(MessageKeys.REGISTER_CHAT_MESSAGE);
                    player.sendMessage(message.asComponent());
                    sendRegisterTitle(titleBarApi, player);
                }
            }
        }
    }

    private void tryAuthenticate(ProxiedPlayer player) {
        this.plugin.getScheduler().async().execute(() -> {
            try {
                this.plugin.getAuthManager().authenticate(
                    player.getPendingConnection());
            } catch (AuthenticationException ex) {
                Message message = this.plugin.getMessageConfig()
                    .get(MessageKeys.FAILED_AUTHENTICATION);
                player.disconnect(message.asComponent());
            }
        });
    }

    private void sendRegisterTitle(TitleBarApi titleBarApi, ProxiedPlayer player) {
        Message title = plugin.getMessageConfig()
                .get(MessageKeys.REGISTER_TITLE);
        Message subtitle = plugin.getMessageConfig()
                .get(MessageKeys.REGISTER_SUBTITLE);
        titleBarApi.send(player, title, subtitle);
    }

    private void sendLoginTitle(TitleBarApi titleBarApi, ProxiedPlayer player) {
        Message title = plugin.getMessageConfig()
                .get(MessageKeys.LOGIN_TITLE);
        Message subtitle = plugin.getMessageConfig()
                .get(MessageKeys.LOGIN_SUBTITLE);
        titleBarApi.send(player, title, subtitle);
    }
}
