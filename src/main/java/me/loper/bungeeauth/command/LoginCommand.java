package me.loper.bungeeauth.command;

import me.loper.bungeeauth.authentication.AttemptCalculator;
import me.loper.bungeeauth.authentication.Authentication;
import me.loper.bungeeauth.config.Message;
import me.loper.bungeeauth.event.PlayerAuthenticatedEvent;
import me.loper.bungeeauth.event.PlayerLoginFailedEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import me.loper.bungeeauth.BungeeAuthPlayer;
import me.loper.bungeeauth.BungeeAuthPlugin;
import me.loper.bungeeauth.config.MessageKeys;

public class LoginCommand extends BungeeAuthCommand {

	private final BungeeAuthPlugin plugin;
	private final AttemptCalculator attemptCalculator;

	public LoginCommand(BungeeAuthPlugin plugin) {
		super(plugin, "login", null, "l");
		this.plugin = plugin;
		this.attemptCalculator = plugin.getAttemptCalculator();
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
				int leftAttempts = this.attemptCalculator.handle(player);
				player.sendMessage(message.asComponent(leftAttempts));
				return;
			case AUTHENTICATION_FAILED:
				Message msg = plugin.getMessageConfig()
						.get(MessageKeys.FAILED_AUTHENTICATION);
				player.disconnect(msg.asComponent());
		}
	}
}