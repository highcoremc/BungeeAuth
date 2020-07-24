package org.nocraft.renay.bungeeauth;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.server.ServerManager;
import org.nocraft.renay.bungeeauth.server.ServerType;
import org.nocraft.renay.bungeeauth.util.TitleBarApi;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncLoginChecker implements Runnable {

    private final Queue<ServerInfo> servers;
    private final BungeeAuthPlugin plugin;
    private long maxAuthTime = 300;

    /**
     * Checks async players on auth servers.
     *
     * @param servers List<String> Login servers
     */
    public AsyncLoginChecker(BungeeAuthPlugin plugin, List<String> servers) {
        this.plugin = plugin;
        this.servers = new ArrayBlockingQueue<>(servers.size());
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
                BungeeAuthPlayer bungeeAuthPlayer = this.plugin.getAuthPlayers()
                        .get(player.getUniqueId());

                if (bungeeAuthPlayer == null) {
                    return;
                }

                long timeLeft = (now - bungeeAuthPlayer.joinedAt.getTime()) / 1000;
                if (timeLeft >= this.maxAuthTime) {
                    Message message = plugin.getMessageConfig().get(
                            MessageKeys.LOGIN_TIMEOUT);
                    player.disconnect(message.asComponent());
                    continue;
                }

                if (bungeeAuthPlayer.isAuthenticated()) {
                    ServerManager serverManager = this.plugin.getServerManager();
                    ServerType serverType = serverManager.getServerType(
                            player.getServer().getInfo());
                    if (serverType.equals(ServerType.LOGIN)) {
                        serverManager.connect(ServerType.GAME, player);
                    }
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
