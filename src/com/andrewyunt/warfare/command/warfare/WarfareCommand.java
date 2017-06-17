package com.andrewyunt.warfare.command.warfare;

import com.andrewyunt.warfare.command.warfare.arguments.*;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.ArgumentExecutor;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.chat.*;
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
			sender.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "FaithfulMC " + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW
					+ ChatColor.RED + " You do not have permission to execute this command.");
			return false;
		}

		if (args.length < 1) {
			sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
			sender.sendMessage(BaseConstants.GOLD + ChatColor.BOLD.toString() + WordUtils.capitalizeFully(command.getName()) + " Help");
			for (final CommandArgument argument : this.arguments) {
				final String permission = argument.getPermission();
				if (permission == null || sender.hasPermission(permission)) {
					ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, argument.getUsage(label));
					HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent
							.fromLegacyText(BaseConstants.YELLOW + "Click to run " + BaseConstants.GRAY + argument.getUsage(label)));
					BaseComponent[] components = new ComponentBuilder(argument.getUsage(command.getName()))
							.color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).event(clickEvent).event(hoverEvent)
							.append(" - " + argument.getDescription()).event(clickEvent).event(hoverEvent).create();
					if (sender instanceof Player) {
						((Player)sender).spigot().sendMessage(components);
					} else {
						sender.sendMessage(BaseComponent.toLegacyText(components));
					}
				}
			}
			sender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
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