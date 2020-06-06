package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.nocraft.renay.bungeeauth.Authentication;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.event.LoginSuccessfulEvent;

import static org.nocraft.renay.bungeeauth.Authentication.Result.*;

public class LoginCommand extends BungeeAuthCommand {

	private final BungeeAuthPlugin plugin;

	public LoginCommand(BungeeAuthPlugin plugin) {
		super(plugin, "login", null, "l");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Only player can run this command."));
			return;
		}

		if (args.length != 1) {
			String usage = "&6&lUSAGE&f: &fUse the &7/l &c<password>&f for authenticate.";
			String transformed = ChatColor.translateAlternateColorCodes('&', usage);
			sender.sendMessage(TextComponent.fromLegacyText(transformed));
			return;
		}

		String password = args[0];
		ProxiedPlayer player = (ProxiedPlayer) sender;
		Authentication.Result result = plugin.authenticate(player.getUniqueId(), password);

		switch (result) {
			case SUCCESS_LOGIN:
				String msg = ChatColor.GREEN + "Successfully authenticated!";
				player.sendMessage(TextComponent.fromLegacyText(msg));

				BungeeAuthPlayer bap = this.plugin.getAuthPlayers().get(player.getUniqueId());
				this.plugin.updateAuthSession(bap).thenAccept(s -> {
					this.plugin.getPluginManager().callEvent(
							new LoginSuccessfulEvent(player.getUniqueId()));
				});
				return;
			case WRONG_PASSWORD:
				String wrong = ChatColor.RED + "Wrong password, you have %d attempts..";
				sender.sendMessage(TextComponent.fromLegacyText(String.format(wrong, 1)));
				return;
			case ALREADY_AUTHENTICATED:
				return;
			case ACCOUNT_NOT_FOUND:
				player.disconnect(TextComponent.fromLegacyText("Please rejoin to the server."));
		}
	}
}