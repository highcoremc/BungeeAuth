package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.authentication.AttemptManager;
import org.nocraft.renay.bungeeauth.authentication.Authentication;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.event.PlayerLoginFailedEvent;
import org.nocraft.renay.bungeeauth.event.PlayerAuthenticatedEvent;

public class LoginCommand extends BungeeAuthCommand {

	private final BungeeAuthPlugin plugin;
	private final AttemptManager attemptManager;

	public LoginCommand(BungeeAuthPlugin plugin) {
		super(plugin, "login", null, "l");
		this.plugin = plugin;
		this.attemptManager = plugin.getAttemptManager();
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
				try {
					BungeeAuthPlayer bap = this.plugin.getAuthPlayers().get(player.getUniqueId());
					PlayerAuthenticatedEvent event = new PlayerAuthenticatedEvent(player.getUniqueId());

					this.plugin.updateAuthSession(bap).thenAccept(s ->
							this.plugin.getPluginManager().callEvent(event));
				} catch (IllegalStateException ex) {
					ex.printStackTrace();
					PlayerLoginFailedEvent event = new PlayerLoginFailedEvent(player.getUniqueId());
					this.plugin.getPluginManager().callEvent(event);
				}
				return;
			case WRONG_PASSWORD:
				this.handleWrongPassword(player, this.attemptManager.handle(player));
				return;
			case ACCOUNT_NOT_FOUND: handleAccountNotFound(player);
		}
	}

	private void handleWrongPassword(ProxiedPlayer player, int attemptsLeft) {
		String message = ChatColor.translateAlternateColorCodes('&',
				"\n&c&lFAILURE&f: Wrong password. You have &e" + attemptsLeft + "&r attempts.\n");
		player.sendMessage(TextComponent.fromLegacyText(message));
	}

	private void handleAccountNotFound(ProxiedPlayer player) {
		String message = ChatColor.translateAlternateColorCodes('&',
				"&6&lFAILURE AUTHENTICATION\n" +
				"&fSorry for the inconvenience,\n"+
				"&fPlease re-enter to the server");
		player.disconnect(TextComponent.fromLegacyText(message));
	}
}