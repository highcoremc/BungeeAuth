package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.event.ChangedPasswordEvent;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.entity.UserPassword;

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
        if (args.length < 3) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.CHANGEPASSWORD_PLAYER_USAGE);
            player.sendMessage(message.asComponent());
            return;
        }

        String newPassword = args[0];
        String confirmPassword = args[1];

        if (!newPassword.equals(confirmPassword)) {
            Message message = plugin.getMessageConfig().get(
                MessageKeys.CHANGEPASSWORD_WRONG_CONFIRM);
            player.sendMessage(message.asComponent());
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
                MessageKeys.CHANGEPASSWORD_CONSOLE_USAGE);
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
            optionalUser = this.plugin.getDataStorage().loadUser(playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        if (!optionalUser.isPresent()) {
            Message message = this.plugin.getMessageConfig()
                .get(MessageKeys.PLAYER_ACCOUNT_NOT_FOUND);
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
