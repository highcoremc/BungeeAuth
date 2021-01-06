package org.nocraft.renay.bungeeauth.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.nocraft.renay.bungeeauth.authentication.AttemptManager;
import org.nocraft.renay.bungeeauth.authentication.Authentication;
import org.nocraft.renay.bungeeauth.BungeeAuthPlayer;
import org.nocraft.renay.bungeeauth.BungeeAuthPlugin;
import org.nocraft.renay.bungeeauth.config.Message;
import org.nocraft.renay.bungeeauth.config.MessageKeys;
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
			Message message = plugin.getMessageConfig()
					.get(MessageKeys.LOGIN_USAGE);
			sender.sendMessage(message.asComponent());
			return;
		}

		String password = args[0];
		ProxiedPlayer player = (ProxiedPlayer) sender;
		Authentication.Result result = plugin.authenticate(player.getUniqueId(), password);

		switch (result) {
			case SUCCESS_LOGIN:
				try {
					BungeeAuthPlayer bap = this.plugin.getAuthPlayer(player.getUniqueId());
					PlayerAuthenticatedEvent event = new PlayerAuthenticatedEvent(player.getUniqueId());

					this.plugin.updateAuthSession(bap).thenAccept(s -> this.plugin.getPluginManager().callEvent(event));
				} catch (IllegalStateException ex) {
					ex.printStackTrace();
					PlayerLoginFailedEvent event = new PlayerLoginFailedEvent(player.getUniqueId());
					this.plugin.getPluginManager().callEvent(event);
				}
				return;
			case WRONG_PASSWORD:
				Message message = plugin.getMessageConfig()
						.get(MessageKeys.WRONG_PASSWORD);
				int leftAttempts = this.attemptManager.handle(player);
				player.sendMessage(message.asComponent(leftAttempts));
				return;
			case AUTHENTICATION_FAILED:
				Message msg = plugin.getMessageConfig()
						.get(MessageKeys.FAILED_AUTHENTICATION);
				player.disconnect(msg.asComponent());
		}
	}
}