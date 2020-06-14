package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
import org.nocraft.renay.bungeeauth.event.PlayerRegisterFailedEvent;
import org.nocraft.renay.bungeeauth.event.PlayerRegisteredEvent;
import org.nocraft.renay.bungeeauth.storage.entity.User;
import org.nocraft.renay.bungeeauth.storage.entity.UserPassword;

import java.util.UUID;

public class RegisterCommand extends BungeeAuthCommand {

    private final BungeeAuthPlugin plugin;
    private final Integer minLengthPassword;

    public RegisterCommand(BungeeAuthPlugin plugin) {
        super(plugin, "register", null, "reg");
        this.plugin = plugin;
        this.minLengthPassword = plugin.getConfiguration()
                .get(ConfigKeys.MIN_PASSWORD_LENGTH);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Only player can run this command."));
            return;
        }

        if (args.length == 0) {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.LOGIN_USAGE);
            sender.sendMessage(message.asComponent());
            return;
        }

        if (args.length == 2 && !args[0].equals(args[1])) {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.PASSWORD_MISMATCH);
            sender.sendMessage(message.asComponent());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String rawPassword = args[0];

        if (minLengthPassword > rawPassword.length()) {
            Message message = plugin.getMessageConfig()
                    .get(MessageKeys.PASSWORD_MIN_LENGTH);
            sender.sendMessage(message.asComponent(minLengthPassword));
            return;
        }

        BungeeAuthPlayer authPlayer = this.plugin.getAuthPlayers()
                .get(player.getUniqueId());

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
                .thenAccept(s -> this.applySuccessfulRegister(uniqueId))
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