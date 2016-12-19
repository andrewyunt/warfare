/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Andrew Yunt. The intellectual and technical concepts contained
 * herein are proprietary to Andrew Yunt and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Andrew Yunt. Access to the source code contained herein is hereby forbidden to anyone except current Andrew Yunt and those who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ANDREW YUNT IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.andrewyunt.skywarfare.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.objects.Arena;
import com.andrewyunt.skywarfare.objects.Game;
import com.andrewyunt.skywarfare.objects.GamePlayer;

/**
 * The arena command class which is used as a Bukkit CommandExecutor.
 * 
 * @author Andrew Yunt
 */
public class SWCommand implements CommandExecutor {
	
	private static final List<String> help = new ArrayList<String>();

	static {
		help.add(ChatColor.DARK_GRAY + "=" + ChatColor.GRAY + "------------" + ChatColor.DARK_GRAY + "[ " + ChatColor.AQUA + 
				"SkyWarfare Help" + ChatColor.DARK_GRAY + " ]" + ChatColor.GRAY + "------------" + ChatColor.DARK_GRAY + "=");
		help.add(ChatColor.GREEN + "/sw edit");
		help.add(ChatColor.GREEN + "/sw addcage " + ChatColor.AQUA + "[name]");
		help.add(ChatColor.GREEN + "/sw removecage " + ChatColor.AQUA + "[name]");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!cmd.getName().equalsIgnoreCase("sw"))
			return false;
		
		if (!(args.length > 0)) {
			
			if (!sender.hasPermission("skywarfare.help")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			for (String line : help)
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
			
			return false;
		}
	
		if (args[0].equalsIgnoreCase("addcoins")) {
			
			if (sender instanceof Player) {
				System.out.println("You may only execute that command from the console.");
				return false;
			}
			
			if (!(args.length >= 3)) {
				sender.sendMessage(ChatColor.RED + "Usage: /sw addcoins [player] [amount]");
				return false;
			}
			
			Player coinsPlayer = Bukkit.getServer().getPlayer(args[1]);
			GamePlayer coinsGP = null;
			
			try {
				coinsGP = SkyWarfare.getInstance().getPlayerManager().getPlayer(coinsPlayer.getName());
			} catch (PlayerException e) {
			}
			
			int coins = 0;
			
			try {
				coins = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Usage: /sw addcoins [player] [amount]");
				return false;
			}
			
			coinsGP.setCoins(coinsGP.getCoins() + coins);
			coinsGP.getBukkitPlayer().sendMessage(ChatColor.GREEN + String.format("You received %s coins from %s.", 
					ChatColor.AQUA + String.valueOf(coins) + ChatColor.GREEN ,
					ChatColor.AQUA + sender.getName() + ChatColor.GREEN));
			
			return true;
		}
		
		if (!(sender instanceof Player)) {
			System.out.println("You may not execute that command from the console.");
			return false;
		}
		
		if (args[0].equalsIgnoreCase("help")) {
			
			if (!sender.hasPermission("skywarfare.help")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			for (String line : help)
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
			
			return false;
		
		} else if (args[0].equalsIgnoreCase("addcage")) {
			
			if (!sender.hasPermission("skywarfare.addcage")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /sw addcage [name]");
				return false;
			}
			
			Arena arena = SkyWarfare.getInstance().getArena();
			
			if (!arena.isEdit()) {
				sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
				sender.sendMessage(ChatColor.RED + "Usage: /sw edit");
				return false;
			}
			
			if (arena.getCageLocations().containsKey(args[1])) {
				sender.sendMessage(ChatColor.RED + "A cage with that name already exists.");
				return false;
			}
			
			Location loc = ((Player) sender).getLocation();
			
			arena.addCageLocation(args[1], loc);
			arena.save();
			
			sender.sendMessage(String.format(ChatColor.GREEN + "You created the cage %s at %s.", 
					ChatColor.AQUA + args[1] + ChatColor.GREEN,
					String.format("X:%s Y:%s Z:%s world: %s", 
							ChatColor.AQUA + String.valueOf(loc.getX()) + ChatColor.GREEN,
							ChatColor.AQUA + String.valueOf(loc.getY()) + ChatColor.GREEN,
							ChatColor.AQUA + String.valueOf(loc.getZ()) + ChatColor.GREEN,
							ChatColor.AQUA + loc.getWorld().getName())  + ChatColor.GREEN));
		
		} else if (args[0].equalsIgnoreCase("removecage")) {
			
			if (!sender.hasPermission("skywarfare.removecage")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /sw removecage [name]");
				return false;
			}
			
			Arena arena = SkyWarfare.getInstance().getArena();
			
			if (!arena.isEdit()) {
				sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
				sender.sendMessage(ChatColor.RED + "Usage: /sw edit");
				return false;
			}
			
			if (arena.getCageLocations().containsKey(args[1])) {
				sender.sendMessage(ChatColor.RED + "A cage with that name already exists.");
				return false;
			}
			
			arena.getCageLocations().remove(args[1]);
			arena.save();
			
			sender.sendMessage(String.format(ChatColor.GREEN + "You removed the cage %s.",
					ChatColor.AQUA + args[1] + ChatColor.GREEN));
			
		} else if (args[0].equalsIgnoreCase("edit")) {
			
			if (!sender.hasPermission("skywarfare.edit")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			Arena arena = SkyWarfare.getInstance().getArena();
			
			if (arena == null)
				return false;
			
			if (arena.isEdit()) {
				arena.setEdit(false);
				
				SkyWarfare.getInstance().setGame(new Game());
				
				sender.sendMessage(ChatColor.GREEN + "You have disabled edit mode for the arena.");
			} else {
				Game game = SkyWarfare.getInstance().getGame();
				
				if (game != null)
					game.end();
				
				arena.setEdit(true);
				sender.sendMessage(ChatColor.GREEN + "You have enabled edit mode for the arena.");
			}
		} else if (args[0].equalsIgnoreCase("start")) {
			
			if (!sender.hasPermission("skywarfare.start")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			Game game = SkyWarfare.getInstance().getGame();
			
			game.setStage(Game.Stage.WAITING);
			
		} else if (args[0].equalsIgnoreCase("restart")) {
			
			if (!sender.hasPermission("skywarfare.restart")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			Game game = SkyWarfare.getInstance().getGame();
			
			game.setStage(Game.Stage.RESTART);
		}
		
		return true;
	}
}