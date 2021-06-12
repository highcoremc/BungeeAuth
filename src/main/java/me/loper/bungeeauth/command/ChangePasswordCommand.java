package me.loper.bungeeauth.command;

import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.event.ChangedPasswordEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;
import me.loper.bungeeauth.BungeeAuthPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.UserPassword;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ChangePasswordCommand extends BungeeAuthCommand implements TabExecutor {

    private final BungeeAuthPlugin plugin;

    public ChangePasswordCommand(BungeeAuthPlugin plugin) {
        super(plugin, "changepassword", null, "changepw", "chpw");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            this.asPlayer((ProxiedPlayer) sender, args);
            return;
        }

        this.asConsole(sender, args);
    }

    private void asPlayer(ProxiedPlayer player, String[] args) {
        if (args.length < 2) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.CHANGEPASSWORD_SELF_USAGE);
            player.sendMessage(message.asComponent());
            return;
        }

        int minLengthPassword = plugin.getConfiguration()
            .get(ConfigKeys.MIN_PASSWORD_LENGTH);

        String newPassword = args[0];
        String confirmPassword = args[1];

        if (!newPassword.equals(confirmPassword)) {
            Message message = plugin.getMessageConfig()
                .get(MessageKeys.PASSWORD_MISMATCH);
            player.sendMessage(message.asComponent());
            return;
        }

        if (minLengthPassword > newPassword.length()) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.PASSWORD_MIN_LENGTH);
            player.sendMessage(message.asComponent(minLengthPassword));
            return;
        }

        BungeeAuthPlayer authPlayer =
            this.plugin.getAuthPlayer(player.getUniqueId());
        UserPassword password = this.plugin.getAuthFactory()
            .createUserPassword(player, newPassword);
        authPlayer.user.changePassword(password);

        ChangedPasswordEvent event = new ChangedPasswordEvent(
            player, player.getUniqueId(), player.getName());

        this.plugin.getDataStorage().changeUserPassword(authPlayer.user.getPassword())
            .thenRun(() -> this.plugin.getPluginManager().callEvent(event));
    }

    private void asConsole(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.CHANGEPASSWORD_OTHER_USAGE);
            sender.sendMessage(message.asComponent());
            return;
        }

        String playerName = args[0];
        String newPassword = args[1];

        this.plugin.getScheduler().async().execute(() -> handle(sender, playerName, newPassword));
    }

    private void handle(CommandSender sender, String playerName, String newPassword) {
        Optional<User> optionalUser;

        try {
            optionalUser = this.plugin.loadUser(playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        if (!optionalUser.isPresent()) {
            Message message = this.plugin.getMessageConfig()
                .get(MessageKeys.ACCOUNT_NOT_REGISTERED);
            sender.sendMessage(message.asComponent(playerName));
            return;
        }

        User user = optionalUser.get();

        UserPassword password = this.plugin.getAuthFactory()
            .createUserPassword(user, newPassword);
        user.changePassword(password);

        try {
            this.plugin.getDataStorage().changeUserPassword(user.getPassword()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        this.plugin.getPluginManager().callEvent(new ChangedPasswordEvent(sender, user.uniqueId, playerName));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer) || 2 < args.length) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        String lastArg = args[args.length - 1];

        if (2 == args.length) {
            result.add(args[0]);
        }

        return result.stream().filter(r -> r.startsWith(lastArg))
            .collect(Collectors.toList());
    }
}
