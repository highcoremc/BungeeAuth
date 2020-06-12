package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.ConfigKeys;
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
            String usage = "&6&lUSAGE&f: &fUse the &7/reg &c<password>&f for register.";
            String transformed = ChatColor.translateAlternateColorCodes('&', usage);
            sender.sendMessage(TextComponent.fromLegacyText(transformed));
            return;
        }

        if (args.length == 2 && !args[0].equals(args[1])) {
            String usage = "&c&lFAILURE&f: &fPassword confirmation does not matches.";
            String transformed = ChatColor.translateAlternateColorCodes('&', usage);
            sender.sendMessage(TextComponent.fromLegacyText(transformed));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String rawPassword = args[0];

        if (minLengthPassword > rawPassword.length()) {
            String usage = "&c&lFAILURE&f: &fPassword length can not be less then &e%s&f.";
            String message = String.format(usage, minLengthPassword);
            String transformed = ChatColor.translateAlternateColorCodes('&', message);
            sender.sendMessage(TextComponent.fromLegacyText(transformed));
            return;
        }

        BungeeAuthPlayer authPlayer = this.plugin.getAuthPlayers()
                .get(player.getUniqueId());

        if (authPlayer.user.hasPassword()) {
            String usage = "&c&lFAILURE&f: &fYou have already registered.";
            String transformed = ChatColor.translateAlternateColorCodes('&', usage);
            sender.sendMessage(TextComponent.fromLegacyText(transformed));
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