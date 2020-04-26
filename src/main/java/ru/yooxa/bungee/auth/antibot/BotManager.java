package ru.yooxa.bungee.auth.antibot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import ru.yooxa.bungee.auth.AuthManager;
import ru.yooxa.bungee.auth.CommandEvent;
import ru.yooxa.bungee.auth.Main;

public class BotManager implements Listener {
    private long time;
    private int limit;
    private Map<String, Client> clients;

    public BotManager(AuthManager manager)
    {
        this.clients = new HashMap();

        startScheduler();

        this.time = manager.getConfig().getLong("AntiBot.banTime");
        this.limit = manager.getConfig().getInt("AntiBot.maxNames");

        Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), this);
    }

    @EventHandler
    public void onCommand(CommandEvent e)
    {
        if (!e.isCancelled() &&
                e.getCommand().toLowerCase().equals("antibot")) {
            int bots = 0;
            int suspicious = 0;

            for (Client client : this.clients.values()) {
                if (client.getNames().size() >= this.limit) {
                    bots++;
                    continue;
                }
                if (this.limit - client.getNames().size() == 1) {
                    suspicious++;
                }
            }

            e.getPlayer().sendMessage("          §f[§cAntiBot§f]");
            e.getPlayer().sendMessage("§c* §fОбнаружено ботов - §c" + bots);
            e.getPlayer().sendMessage("§c* §fНа проверке - §c" + (this.clients.size() - bots));
            e.getPlayer().sendMessage("§c* §fПод подозрением - §c" + suspicious);
            e.used();
        }
    }


    public void checkClient(PreLoginEvent e)
    {
        String name = e.getConnection().getName();
        String ip = e.getConnection().getAddress().getHostString();
        Client client = (Client) this.clients.get(ip);
        if (client == null) {
            client = new Client(name.toLowerCase(), ip);
            this.clients.put(ip, client);
        } else {
            Set temp = client.getNames();
            if (!temp.contains(name.toLowerCase())) {
                if (temp.size() >= this.limit) {
                    String nicks = "";


                    for (String nick : client.getNames()) nicks = nicks + "\n§a" + nick;


                    e.setCancelled(true);
                    e.setCancelReason("§eВы сменили слишком много ников за короткое время\n§eДопустимые ники:" + nicks);
                } else {
                    client.getNames().add(name.toLowerCase());
                }
            }
        }
    }


    public void startScheduler()
    {
        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
            while (true) {
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException var2) {
                    var2.printStackTrace();
                }

                BotManager.this.check();
            }
        }, 0L, TimeUnit.SECONDS);
    }


    public void check()
    {
        HashMap<String, Client> temp = new HashMap<String, Client>();

        for (Client client : this.clients.values()) {
            if (System.currentTimeMillis() / 1000L - client.getTime() < this.time) {
                temp.put(client.getIp(), client);
            }
        }

        this.clients = temp;
    }
}


