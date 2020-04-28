package ru.yooxa.bungee.auth;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import ru.yooxa.bungee.auth.hash.RandomString;

public class ChatListener implements Listener {
    String whitelist = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_\u00b8abcdefghijklmnopqrstuvwxyz{|}~\u0410\u0411\u0412\u0413\u0414\u0415\u0401\u0416\u0417\u0418\u0419\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042b\u042d\u042e\u042f\u0430\u0431\u0432\u0433\u0434\u0435\u0451\u0436\u0437\u0438\u0439\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044a\u044b\u044c\u044d\u044e\u044f";
    String mailWhiteList = "abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ.@0123456789";
    private AuthManager manager;

    public ChatListener(AuthManager manager) {
        this.manager = manager;
    }

    private boolean checkMessage(String allowed, String message) {
        for (int i = 0; i < message.length(); ++i) {
            if (allowed.contains(String.valueOf(message.charAt(i))))
                continue;
            return false;
        }
        return true;
    }

    @EventHandler(priority = 64)
    public void onChat(ChatEvent e) {
        if (e.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) e.getSender();
            AuthPlayer player = this.manager.getPlayer(p);
            if (player == null) {
                e.setCancelled(true);
            } else if (!e.getMessage().startsWith("/")) {
                if (!player.isAuth()) {
                    e.setCancelled(true);
                }
            } else {
                String[] message = e.getMessage().split(" ");
                String command = message[0].replaceFirst("/", "");
                String[] args = new String[message.length - 1];
                System.arraycopy(message, 1, args, 0, message.length - 1);
                CommandEvent event = new CommandEvent((ProxiedPlayer) e.getSender(), command, args);
                Main.getInstance().getProxy().getPluginManager().callEvent((Event) event);
                if (event.isUsed()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(CommandEvent e) {
        ProxiedPlayer player = e.getPlayer();
        AuthPlayer authPlayer = this.manager.getPlayer(player);
        if (authPlayer == null) {
            e.setCanceled(true);
            e.used();
        } else {
            String command;
            block17:
            switch (command = e.getCommand().toLowerCase()) {
                case "auth": {
                    String subCommand;
                    e.used();
                    if (!authPlayer.isAuth())
                        break;
                    if (!this.manager.admins.contains(player.getName())) {
                        player.sendMessage(new TextComponent("\u00a7c\u0423 \u0432\u0430\u0441 \u043d\u0435\u0442 \u043f\u0440\u0430\u0432"));
                        return;
                    }
                    if (e.getArgs().length == 1 && e.getArgs()[0].equalsIgnoreCase("reload")) {
                        this.manager.loadConfig();
                        this.manager.ipProtect.load(this.manager.getConfig());
                        player.sendMessage(new TextComponent("\u00a7c* \u00a7f\u041a\u043e\u043d\u0444\u0438\u0433 \u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d"));
                        break;
                    }
                    if (e.getArgs().length < 2 || e.getArgs()[0].equalsIgnoreCase("help")) {
                        player.sendMessage(new TextComponent("\u041f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433 - /auth reload"));
                        player.sendMessage(new TextComponent("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 - /auth register [\u043d\u0438\u043a] [\u043f\u0430\u0440\u043e\u043b\u044c]"));
                        player.sendMessage(new TextComponent("\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044e - /auth unregister [\u043d\u0438\u043a]"));
                        player.sendMessage(new TextComponent("\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u0430\u0440\u043e\u043b\u044c - /auth changepassword [\u043d\u0438\u043a] [\u043f\u0430\u0440\u043e\u043b\u044c]"));
                        player.sendMessage(new TextComponent("\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - /auth changemail [\u043d\u0438\u043a] [\u043f\u043e\u0447\u0442\u0430]"));
                        player.sendMessage(new TextComponent("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u0432\u0445\u043e\u0434 - /auth lastlogin [\u043d\u0438\u043a]"));
                        return;
                    }
                    switch (subCommand = e.getArgs()[0]) {
                        case "lastlogin": {
                            if (e.getArgs().length < 2) {
                                player.sendMessage(new TextComponent("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 - /auth register [\u043d\u0438\u043a] [\u043f\u0430\u0440\u043e\u043b\u044c]"));
                                break block17;
                            }
                            String pass = e.getArgs()[1];
                            try {
                                long pass2 = this.manager.lastLogin(pass);
                                player.sendMessage(new TextComponent("\u0418\u0433\u0440\u043e\u043a " + pass + " \u043f\u043e\u0441\u043b\u0435\u0434\u043d\u0438\u0439 \u0440\u0430\u0437 \u0431\u044b\u043b \u0432 \u0441\u0435\u0442\u0438 " + Utils.leftTime(pass2, false)));
                            } catch (Exception var16) {
                                player.sendMessage(new TextComponent("\u041e\u0448\u0438\u0431\u043a\u0430 - " + var16.getMessage()));
                                var16.printStackTrace();
                            }
                            break block17;
                        }
                        case "register": {
                            if (e.getArgs().length < 3) {
                                player.sendMessage(new TextComponent("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 - /auth register [\u043d\u0438\u043a] [\u043f\u0430\u0440\u043e\u043b\u044c]"));
                                break block17;
                            }
                            String pass = e.getArgs()[1];
                            subCommand = e.getArgs()[2];
                            ProxiedPlayer p1 = Main.getInstance().getProxy().getPlayer(pass);
                            if (p1 != null) {
                                p1.disconnect(new BaseComponent[0]);
                            }
                            try {
                                this.manager.register(pass, subCommand);
                                player.sendMessage(new TextComponent("\u0423\u0441\u043f\u0435\u0448\u043d\u043e"));
                            } catch (Exception var15) {
                                player.sendMessage(new TextComponent("\u041e\u0448\u0438\u0431\u043a\u0430 - " + var15.getMessage()));
                                var15.printStackTrace();
                            }
                            break block17;
                        }
                        case "unregister": {
                            if (e.getArgs().length < 2) {
                                player.sendMessage(new TextComponent("\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044e - /auth unregister [\u043d\u0438\u043a]"));
                                break block17;
                            }
                            String pass = e.getArgs()[1];
                            ProxiedPlayer p1 = Main.getInstance().getProxy().getPlayer(pass);
                            if (p1 != null) {
                                p1.disconnect(new BaseComponent[0]);
                            }
                            try {
                                Thread.sleep(2000L);
                                this.manager.unregister(pass);
                                player.sendMessage(new TextComponent("\u0423\u0441\u043f\u0435\u0448\u043d\u043e"));
                            } catch (Exception var14) {
                                player.sendMessage(new TextComponent("\u041e\u0448\u0438\u0431\u043a\u0430 - " + var14.getMessage()));
                                var14.printStackTrace();
                            }
                            break block17;
                        }
                        case "changepassword": {
                            if (e.getArgs().length < 3) {
                                player.sendMessage(new TextComponent("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 - /auth register [\u043d\u0438\u043a] [\u043f\u0430\u0440\u043e\u043b\u044c]"));
                                break block17;
                            }
                            String pass = e.getArgs()[1];
                            subCommand = e.getArgs()[2];
                            ProxiedPlayer p1 = Main.getInstance().getProxy().getPlayer(pass);
                            if (p1 != null) {
                                p1.disconnect(new BaseComponent[0]);
                            }
                            try {
                                Thread.sleep(2000L);
                                this.manager.changePassword(pass, subCommand);
                                player.sendMessage(new TextComponent("\u0423\u0441\u043f\u0435\u0448\u043d\u043e"));
                            } catch (Exception var13) {
                                player.sendMessage(new TextComponent("\u041e\u0448\u0438\u0431\u043a\u0430 - " + var13.getMessage()));
                                var13.printStackTrace();
                            }
                            break block17;
                        }
                        case "changemail": {
                            if (e.getArgs().length < 3) {
                                player.sendMessage(new TextComponent("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 - /auth register [\u043d\u0438\u043a] [\u043f\u0430\u0440\u043e\u043b\u044c]"));
                                break block17;
                            }
                            String pass = e.getArgs()[1];
                            String generatedPassword = e.getArgs()[2];
                            ProxiedPlayer p1 = Main.getInstance().getProxy().getPlayer(pass);
                            if (p1 != null) {
                                p1.disconnect(new BaseComponent[0]);
                            }
                            try {
                                Thread.sleep(2000L);
                                this.manager.changeMail(pass, generatedPassword);
                                player.sendMessage(new TextComponent("\u0423\u0441\u043f\u0435\u0448\u043d\u043e"));
                                break block17;
                            } catch (Exception var12) {
                                player.sendMessage(new TextComponent("\u041e\u0448\u0438\u0431\u043a\u0430 - " + var12.getMessage()));
                                var12.printStackTrace();
                            }
                        }
                    }
                    break;
                }
                case "login":
                case "l": {
                    e.used();
                    if (!authPlayer.isRegister()) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u0412\u044b \u043d\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u044b"));
                        return;
                    }
                    if (authPlayer.isAuth()) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7a\u0422\u044b \u0443\u0436\u0435 \u0432 \u0438\u0433\u0440\u0435 \u043b\u0430\u043b\u043a\u0430))"));
                        return;
                    }
                    if (e.getArgs().length != 1) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7e\u0412\u043e\u0439\u0434\u0438\u0442\u0435 - \u00a7c/login \u00a7f[\u00a7c\u043f\u0430\u0440\u043e\u043b\u044c\u00a7f]"));
                        return;
                    }
                    authPlayer.tryAuth(e.getArgs()[0]);
                    break;
                }
                case "register":
                case "reg": {
                    e.used();
                    if (authPlayer.isAuth()) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7a\u0422\u044b \u0443\u0436\u0435 \u0432 \u0438\u0433\u0440\u0435 \u043b\u0430\u043b\u043a\u0430))"));
                        return;
                    }
                    if (e.getArgs().length < 2) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7e\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435\u0441\u044c - \u00a7c/register \u00a7f[\u00a7c\u043f\u0430\u0440\u043e\u043b\u044c\u00a7f] [\u00a7c\u043f\u0430\u0440\u043e\u043b\u044c\u00a7f]"));
                        return;
                    }
                    if (!e.getArgs()[0].equals(e.getArgs()[1])) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u041f\u0430\u0440\u043e\u043b\u0438 \u043e\u0442\u043b\u0438\u0447\u0430\u044e\u0442\u0441\u044f"));
                        return;
                    }
                    if (e.getArgs()[0].length() < 4) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u043f\u0430\u0440\u043e\u043b\u044f - 4 \u0441\u0438\u043c\u0432\u043e\u043b\u0430"));
                        return;
                    }
                    if (e.getArgs()[0].length() > 16) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u043f\u0430\u0440\u043e\u043b\u044f - 16 \u0441\u0438\u043c\u0432\u043e\u043b\u043e\u0432"));
                        return;
                    }
                    if (!this.checkMessage(this.whitelist, e.getArgs()[0])) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u0412 \u043f\u0430\u0440\u043e\u043b\u0435 \u0435\u0441\u0442\u044c \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0435 \u0441\u0438\u043c\u0432\u043e\u043b\u044b!"));
                        return;
                    }
                    authPlayer.register(e.getArgs()[0]);
                    break;
                }
                case "changepassword":
                case "changepass": {
                    if (!authPlayer.isAuth())
                        break;
                    e.used();
                    if (e.getArgs().length != 2) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7e\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u0430\u0440\u043e\u043b\u044c - \u00a7c/changepassword \u00a7f[\u00a7c\u0441\u0442\u0430\u0440\u044b\u0439 \u043f\u0430\u0440\u043e\u043b\u044c\u00a7f] [\u00a7c\u043d\u043e\u0432\u044b\u0439 \u043f\u0430\u0440\u043e\u043b\u044c\u00a7f]"));
                        return;
                    }
                    String subCommand = e.getArgs()[0];
                    String newPass = e.getArgs()[1];
                    if (!authPlayer.checkPassword(subCommand)) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u0412\u044b \u043d\u0435 \u0432\u0435\u0440\u043d\u043e \u0432\u0432\u0435\u043b\u0438 \u0441\u0442\u0430\u0440\u044b\u0439 \u043f\u0430\u0440\u043e\u043b\u044c"));
                        return;
                    }
                    if (newPass.length() < 4) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u043f\u0430\u0440\u043e\u043b\u044f - 4 \u0441\u0438\u043c\u0432\u043e\u043b\u0430"));
                        return;
                    }
                    if (newPass.length() > 16) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0434\u043b\u0438\u043d\u0430 \u043f\u0430\u0440\u043e\u043b\u044f - 16 \u0441\u0438\u043c\u0432\u043e\u043b\u043e\u0432"));
                        return;
                    }
                    if (!this.checkMessage(this.whitelist, newPass)) {
                        player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u0412 \u043d\u043e\u0432\u043e\u043c \u043f\u0430\u0440\u043e\u043b\u0435 \u0435\u0441\u0442\u044c \u043d\u0435\u0434\u043e\u043f\u0443\u0441\u0442\u0438\u043c\u044b\u0435 \u0441\u0438\u043c\u0432\u043e\u043b\u044b!"));
                        return;
                    }
                    String pass = e.getArgs()[1];
                    authPlayer.setPassword(pass);
                    player.sendMessage((BaseComponent) new TextComponent("\u00a7a\u041f\u0430\u0440\u043e\u043b\u044c \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0438\u0437\u043c\u0435\u043d\u0435\u043d"));
                    break;
                }
                case "logout": {
                    e.used();
                    if (!authPlayer.isAuth())
                        break;
                    authPlayer.logout();
                    break;
                }
                case "email":
                case "mail": {
                    String subCommand;
                    e.used();
                    if (e.getArgs().length < 2 || e.getArgs()[0].equalsIgnoreCase("help")) {
                        if (authPlayer.isAuth()) {
                            player.sendMessage(new TextComponent("\u00a7e\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email add \u00a7f[\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f] [\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f]"));
                            player.sendMessage(new TextComponent("\u00a7e\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email change \u00a7f[\u00a7c\u0421\u0422\u0410\u0420\u042b\u0419_EMAIL\u00a7f] [\u00a7c\u041d\u041e\u0412\u042b\u0419_EMAIL\u00a7f]"));
                        }
                        player.sendMessage(new TextComponent("\u00a7e\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u043f\u0430\u0440\u043e\u043b\u044c - \u00a7c/email recovery \u00a7f[\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f]"));
                        return;
                    }
                    switch (subCommand = e.getArgs()[0]) {
                        case "add": {
                            if (e.getArgs().length < 3) {
                                player.sendMessage(new TextComponent("\u00a7e\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email add \u00a7f[\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f] [\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f]"));
                                return;
                            }
                            if (!authPlayer.isAuth())
                                break;
                            if (authPlayer.hasMail()) {
                                player.sendMessage(new TextComponent("\u00a7c\u0423 \u0432\u0430\u0441 \u0443\u0436\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u0430 \u043f\u043e\u0447\u0442\u0430!"));
                                player.sendMessage(new TextComponent("\u00a7e\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email change \u00a7f[\u00a7c\u0421\u0422\u0410\u0420\u042b\u0419_EMAIL\u00a7f] [\u00a7c\u041d\u041e\u0412\u042b\u0419_EMAIL\u00a7f]"));
                                break;
                            }
                            if (!this.checkMessage(this.mailWhiteList, e.getArgs()[1])) {
                                player.sendMessage(new TextComponent("\u00a7c\u041d\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043f\u043e\u0447\u0442\u0430"));
                                break;
                            }
                            if (e.getArgs()[1].contains(".") && e.getArgs()[1].contains("@")) {
                                if (!e.getArgs()[1].equals(e.getArgs()[2])) {
                                    player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u041f\u043e\u0447\u0442\u044b \u043d\u0435 \u0441\u043e\u0432\u043f\u0430\u0434\u0430\u044e\u0442"));
                                    break;
                                }
                                authPlayer.mail = e.getArgs()[1];
                                player.sendMessage(new TextComponent("\u00a7e\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043b\u0438 \u043f\u043e\u0447\u0442\u0443 - \u00a7c" + e.getArgs()[1]));
                                break;
                            }
                            player.sendMessage(new TextComponent("\u00a7c\u041d\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043f\u043e\u0447\u0442\u0430"));
                            break;
                        }
                        case "change": {
                            if (!authPlayer.isAuth())
                                break;
                            if (e.getArgs().length < 3) {
                                player.sendMessage(new TextComponent("\u00a7e\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email change \u00a7f[\u00a7c\u0421\u0422\u0410\u0420\u042b\u0419_EMAIL\u00a7f] [\u00a7c\u041d\u041e\u0412\u042b\u0419_EMAIL\u00a7f]"));
                                break;
                            }
                            if (!authPlayer.hasMail()) {
                                player.sendMessage(new TextComponent("\u00a7c\u0423 \u0432\u0430\u0441 \u0435\u0449\u0435 \u043d\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u0430 \u043f\u043e\u0447\u0442\u0430"));
                                player.sendMessage(new TextComponent("\u00a7e\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email add \u00a7f[\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f] [\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f]"));
                                break;
                            }
                            if (!authPlayer.mail.equalsIgnoreCase(e.getArgs()[1])) {
                                player.sendMessage(new TextComponent("\u00a7c\u041d\u0435\u0432\u0435\u0440\u043d\u043e \u0432\u0432\u0435\u0434\u0435\u043d\u0430 \u0441\u0442\u0430\u0440\u0430\u044f \u043f\u043e\u0447\u0442\u0430"));
                                break;
                            }
                            if (!this.checkMessage(this.mailWhiteList, e.getArgs()[2])) {
                                player.sendMessage(new TextComponent("\u00a7c\u041d\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043f\u043e\u0447\u0442\u0430"));
                                break;
                            }
                            if (e.getArgs()[2].contains(".") && e.getArgs()[2].contains("@")) {
                                authPlayer.mail = e.getArgs()[2];
                                player.sendMessage(new TextComponent("\u00a7e\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0438\u0437\u043c\u0435\u043d\u0438\u043b\u0438 \u043f\u043e\u0447\u0442\u0443 \u043d\u0430 - \u00a7c" + e.getArgs()[2]));
                                break;
                            }
                            player.sendMessage(new TextComponent("\u00a7c\u041d\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043f\u043e\u0447\u0442\u0430"));
                            break;
                        }
                        case "recovery": {
                            if (!authPlayer.isAuth()) {
                                if (!authPlayer.hasMail()) {
                                    player.sendMessage(new TextComponent("\u00a7c\u0423 \u0432\u0430\u0441 \u043d\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u0430 \u043f\u043e\u0447\u0442\u0430"));
                                    return;
                                }
                                if (!authPlayer.mail.equals(e.getArgs()[1])) {
                                    player.sendMessage(new TextComponent("\u00a7c\u041d\u0435\u0432\u0435\u0440\u043d\u0430\u044f \u043f\u043e\u0447\u0442\u0430"));
                                    return;
                                }
                                String generatedPassword = new RandomString(8).nextString();
                                authPlayer.setPassword(generatedPassword);
                                this.manager.savePlayer(player);
                                player.sendMessage(new TextComponent("\u00a7a\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0441 \u043d\u043e\u0432\u044b\u043c \u043f\u0430\u0440\u043e\u043b\u0435\u043c \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e \u043d\u0430 \u043f\u043e\u0447\u0442\u0443"));
                                return;
                            }
                            player.sendMessage(new TextComponent("\u00a7a\u0422\u044b \u0443\u0436\u0435 \u0432 \u0438\u0433\u0440\u0435 \u043b\u0430\u043b\u043a\u0430))"));
                            break;
                        }
                        default: {
                            player.sendMessage(new TextComponent("\u00a7e\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email add \u00a7f[\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f] [\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f]"));
                            player.sendMessage(new TextComponent("\u00a7e\u0421\u043c\u0435\u043d\u0438\u0442\u044c \u043f\u043e\u0447\u0442\u0443 - \u00a7c/email change \u00a7f[\u00a7c\u0421\u0422\u0410\u0420\u042b\u0419_EMAIL\u00a7f] [\u00a7c\u041d\u041e\u0412\u042b\u0419_EMAIL\u00a7f]"));
                            player.sendMessage(new TextComponent("\u00a7e\u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u043f\u0430\u0440\u043e\u043b\u044c - \u00a7c/email recovery \u00a7f[\u00a7c\u0412\u0410\u0428_EMAIL\u00a7f]"));
                            return;
                        }
                    }
                }
                default: {
                    if (authPlayer.isAuth())
                        break;
                    e.used();
                    e.setCanceled(true);
                    player.sendMessage((BaseComponent) new TextComponent("\u00a7c\u0412\u044b \u043d\u0435 \u0430\u0432\u0442\u043e\u0440\u0438\u0437\u043e\u0432\u0430\u043d\u044b!"));
                }
            }
        }
    }
}