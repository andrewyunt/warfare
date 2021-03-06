package com.andrewyunt.warfare.command.warfare;

import com.andrewyunt.warfare.command.warfare.arguments.EditArgument;
import com.andrewyunt.warfare.command.warfare.arguments.RestartArgument;
import com.andrewyunt.warfare.command.warfare.arguments.StartArgument;
import com.andrewyunt.warfare.command.warfare.arguments.ToggleTeamsArgument;
import com.andrewyunt.warfare.command.warfare.arguments.SetTeamSizeArgument;
import com.andrewyunt.warfare.command.warfare.arguments.SetMapLocationArgument;
import com.andrewyunt.warfare.command.warfare.arguments.SetWaitingLocationArgument;
import com.andrewyunt.warfare.command.warfare.arguments.SetTeamSpawnArgument;
import com.andrewyunt.warfare.command.warfare.arguments.RemoveCageArgument;
import com.andrewyunt.warfare.command.warfare.arguments.AddCageArgument;
import com.andrewyunt.warfare.command.warfare.arguments.AddChestArgument;
import com.andrewyunt.warfare.command.warfare.arguments.AddBoosterArgument;
import com.andrewyunt.warfare.command.warfare.arguments.SetLevelArgument;
import com.andrewyunt.warfare.command.warfare.arguments.RemoveCoinsArgument;
import com.andrewyunt.warfare.command.warfare.arguments.AddCoinsArgument;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.utilities.command.ArgumentExecutor;
import com.andrewyunt.warfare.utilities.command.CommandArgument;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarfareCommand extends ArgumentExecutor {

	public WarfareCommand() {
		super("warfare");

		addArgument(new AddCoinsArgument());
		addArgument(new RemoveCoinsArgument());
		addArgument(new SetLevelArgument());
		addArgument(new AddBoosterArgument());

		if (!StaticConfiguration.LOBBY) {
			addArgument(new AddChestArgument());
			addArgument(new AddCageArgument());
			addArgument(new RemoveCageArgument());
			addArgument(new SetTeamSpawnArgument());
			addArgument(new SetWaitingLocationArgument());
			addArgument(new SetMapLocationArgument());
			addArgument(new SetTeamSizeArgument());
			addArgument(new ToggleTeamsArgument());
			addArgument(new EditArgument());
			addArgument(new StartArgument());
			addArgument(new RestartArgument());
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		boolean hasPermission = false;

		for (CommandArgument argument : arguments) {
			if (sender.hasPermission(argument.getPermission())) {
				hasPermission = true;
			}
		}

		if (!hasPermission) {
			sender.sendMessage(ChatColor.RED + " You do not have permission to execute this command.");
			return false;
		}

		if (args.length < 1) {
			sender.sendMessage(ChatColor.DARK_GRAY + "------------------------------");
			sender.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + WordUtils.capitalizeFully(command.getName()) + " Help");
			for (final CommandArgument argument : this.arguments) {
				final String permission = argument.getPermission();
				if (permission == null || sender.hasPermission(permission)) {
					ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, argument.getUsage(label));
					HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent
							.fromLegacyText(ChatColor.YELLOW + "Click to run " + ChatColor.GRAY + argument.getUsage(label)));
					BaseComponent[] components = new ComponentBuilder(argument.getUsage(command.getName()))
							.event(clickEvent).event(hoverEvent)
							.append(" - " + argument.getDescription()).event(clickEvent).event(hoverEvent).create();
					if (sender instanceof Player) {
						((Player)sender).spigot().sendMessage(components);
					} else {
						sender.sendMessage(BaseComponent.toLegacyText(components));
					}
				}
			}
			sender.sendMessage(ChatColor.DARK_GRAY + "------------------------------");
			return false;
		}
		final CommandArgument argument2 = this.getArgument(args[0]);
		final String permission2 = (argument2 == null) ? null : argument2.getPermission();
		if (argument2 == null || (permission2 != null && !sender.hasPermission(permission2))) {
			sender.sendMessage(ChatColor.RED + WordUtils.capitalizeFully(this.label) + " sub-command " + args[0] + " not found.");
			return false;
		}
		argument2.onCommand(sender, command, label, args);
		return true;
	}
}