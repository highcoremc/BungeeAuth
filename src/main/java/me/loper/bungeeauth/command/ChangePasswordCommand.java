package me.loper.bungeeauth.command;

import me.loper.bungeeauth.BungeeAuthPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.authentication.hash.HashMethod;
import me.loper.bungeeauth.authentication.hash.HashMethodFactory;
import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.event.ChangedPasswordEvent;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.UserPassword;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ChangePasswordCommand extends BungeeAuthCommand {

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

        BungeeAuthPlayer authPlayer = this.plugin.getAuthPlayer(player.getUniqueId());

        if (null == authPlayer) {
            this.plugin.getLogger().info(String.format("Player \"%s\" not found in the database.", player.getName()));
            return;
        }

        UserPassword currentPassword = authPlayer.user.getPassword();
        HashMethod method = HashMethodFactory.create(currentPassword.hashMethodType);

        if (!currentPassword.verify(method, args[0])) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.CHANGEPASSWORD_OLD_INVALID);
            player.sendMessage(message.asComponent());
            return;
        }

        int minLengthPassword = plugin.getConfiguration()
            .get(ConfigKeys.MIN_PASSWORD_LENGTH);

        String newPassword = args[1];

        if (minLengthPassword > newPassword.length()) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.PASSWORD_MIN_LENGTH);
            player.sendMessage(message.asComponent(minLengthPassword));
            return;
        }

        UserPassword password = this.plugin.getAuthFactory()
            .createUserPassword(player, newPassword);
        authPlayer.user.changePassword(password);

        ChangedPasswordEvent event = new ChangedPasswordEvent(
            player, player.getUniqueId(), player.getName());

        this.plugin.getDataStorage().changeUserPassword(password)
            .thenRunAsync(() -> this.plugin.getPluginManager().callEvent(event));
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

        this.plugin.loadUser(playerName).thenAccept(optionalUser ->
            handleChangePassword(sender, playerName, newPassword, optionalUser));
    }

    private void handleChangePassword(CommandSender sender, String playerName, String newPassword, Optional<User> optionalUser) {
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
}
