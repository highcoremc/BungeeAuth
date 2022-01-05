package me.loper.bungeeauth.command;

import me.loper.bungeeauth.config.ConfigKeys;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.event.PlayerRegisterFailedEvent;
import me.loper.bungeeauth.event.PlayerRegisteredEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import me.loper.bungeeauth.BungeeAuthPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.config.MessageKeys;
import me.loper.bungeeauth.storage.entity.User;
import me.loper.bungeeauth.storage.entity.UserPassword;

import java.util.UUID;

public class RegisterCommand extends BungeeAuthCommand {

    private final BungeeAuthPlugin plugin;

    public RegisterCommand(BungeeAuthPlugin plugin) {
        super(plugin, "register", null, "reg");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Only player can run this command."));
            return;
        }

        if (args.length == 0) {
            Message message = this.plugin.getMessageConfig()
                    .get(MessageKeys.LOGIN_USAGE);
            sender.sendMessage(message.asComponent());
            return;
        }

        if (args.length == 2 && !args[0].equals(args[1])) {
            Message message = this.plugin.getMessageConfig()
                    .get(MessageKeys.PASSWORD_MISMATCH);
            sender.sendMessage(message.asComponent());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String rawPassword = args[0];

        int minLengthPassword = this.plugin.getConfiguration()
            .get(ConfigKeys.MIN_PASSWORD_LENGTH);

        if (minLengthPassword > rawPassword.length()) {
            Message message = this.plugin.getMessageConfig()
                    .get(MessageKeys.PASSWORD_MIN_LENGTH);
            sender.sendMessage(message.asComponent(minLengthPassword));
            return;
        }

        BungeeAuthPlayer authPlayer = this.plugin.getAuthPlayer(player.getUniqueId());

        if (null == authPlayer) {
            Message message = plugin.getMessageConfig()
                .get(MessageKeys.FAILED_REGISTER);
            player.disconnect(message.asComponent());
            return;
        }

        if (authPlayer.user.hasPassword()) {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.ALREADY_REGISTERED);
            sender.sendMessage(message.asComponent());
            return;
        }

        UserPassword password = this.plugin.getAuthFactory()
                .createUserPassword(player, rawPassword);
        authPlayer.user.changePassword(password);

        UUID uniqueId = authPlayer.user.uniqueId;
        User user = authPlayer.user;

        this.plugin.getDataStorage().saveUser(user)
                .thenRun(() -> this.plugin.getDataStorage().changeUserPassword(user.getPassword()))
                .thenRun(() -> this.applySuccessfulRegister(uniqueId))
                .exceptionally(s -> this.applyFailedRegister(uniqueId));
    }

    private Void applyFailedRegister(UUID uniqueId) {
        this.plugin.getPluginManager().callEvent(
                new PlayerRegisterFailedEvent(uniqueId));
        return null;
    }

    private void applySuccessfulRegister(UUID uniqueId) {
        this.plugin.getPluginManager().callEvent(
                new PlayerRegisteredEvent(uniqueId));
    }
}