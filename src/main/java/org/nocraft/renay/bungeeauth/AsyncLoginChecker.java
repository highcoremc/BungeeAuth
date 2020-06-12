package org.nocraft.renay.bungeeauth;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.util.TitleBarApi;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncLoginChecker implements Runnable {

    private final Queue<ServerInfo> servers;
    private final BungeeAuthPlugin plugin;
    private long maxAuthTime = 300;

    private final String loginTitle = "&a&l» &2&lWelcome back &a&l«";
    private final String loginSubtitle = "&fPlease login with &2&l/l &7<password>";
    private final String registerTitle = "&a&l» &2&lWelcome to &f&lNocraft &a&l«";
    private final String registerSubtitle = "&fPlease register with &2&l/reg &7<password>";

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
                    String message = ChatColor.translateAlternateColorCodes('&',
                            "&c&lFAILURE AUTHENTICATION\n\n" +
                            "&fSorry, but you were not able to authenticate in 5 minutes.");
                    player.disconnect(TextComponent.fromLegacyText(message));
                    continue;
                }

                if (bungeeAuthPlayer.isAuthenticated()) {
                    ServerManager serverManager = this.plugin.getServerManager();
                    ServerType serverType = serverManager
                            .getServerType(player.getServer().getInfo());
                    if (serverType.equals(ServerType.LOGIN)) {
                        serverManager.connect(ServerType.GAME, player);
                    }
                }

                if (bungeeAuthPlayer.user.isRegistered()) {
                    String chatMessage = ChatColor.translateAlternateColorCodes('&',
                            "&a&lINFO&f: &fPlease login with &2/l &7<password>");
                    player.sendMessage(TextComponent.fromLegacyText(chatMessage));
                    titleBarApi.send(player, this.loginTitle, this.loginSubtitle);
                } else {
                    String chatMessage = ChatColor.translateAlternateColorCodes('&',
                            "&a&lINFO&f: &fPlease register with &2/reg &7<password>");
                    player.sendMessage(TextComponent.fromLegacyText(chatMessage));
                    titleBarApi.send(player, this.registerTitle, this.registerSubtitle);
                }
            }
        }
    }
}
