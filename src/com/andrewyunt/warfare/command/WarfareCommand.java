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
package com.andrewyunt.warfare.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.Arena;
import com.andrewyunt.warfare.objects.Game;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.LootChest;

/**
 * The arena command class which is used as a Bukkit CommandExecutor.
 * 
 * @author Andrew Yunt
 */
public class WarfareCommand implements CommandExecutor {
	
	private static final List<String> help = new ArrayList<String>();

	static {
		help.add(ChatColor.DARK_GRAY + "=" + ChatColor.GRAY + "------------" + ChatColor.DARK_GRAY + "[ " + ChatColor.GOLD + 
				"Warfare Help" + ChatColor.DARK_GRAY + " ]" + ChatColor.GRAY + "------------" + ChatColor.DARK_GRAY + "=");
		help.add(ChatColor.GOLD + "/warfare edit");
		help.add(ChatColor.GOLD + "/warfare addcage " + "[name]");
		help.add(ChatColor.GOLD + "/warfare removecage " + "[name]");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!cmd.getName().equalsIgnoreCase("warfare"))
			return false;
		
		if (!(args.length > 0)) {
			
			if (!sender.hasPermission("warfare.help")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			for (String line : help)
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
			
			return false;
		}
		
		if (!(sender instanceof Player)) {
			System.out.println("You may not execute that command from the console.");
			return false;
		}
		
		Player player = (Player) sender;
		
		if (args[0].equalsIgnoreCase("help")) {
			
			if (!sender.hasPermission("warfare.help")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			for (String line : help)
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
			
			return false;
		
		} else if (args[0].equalsIgnoreCase("addcage")) {
			
			if (!sender.hasPermission("warfare.addcage")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare addcage [name]");
				return false;
			}
			
			Arena arena = Warfare.getInstance().getArena();
			
			if (!arena.isEdit()) {
				sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
				sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
				return false;
			}
			
			if (arena.getCageLocations().containsKey(args[1])) {
				sender.sendMessage(ChatColor.RED + "A cage with that name already exists.");
				return false;
			}
			
			Location loc = player.getLocation();
			
			arena.addCageLocation(args[1], loc);
			arena.save();
			
			sender.sendMessage(String.format(ChatColor.GOLD + "You created the cage %s at %s.", 
					args[1],
					String.format("X:%s Y:%s Z:%s world: %s", 
							String.valueOf(loc.getX()),
							String.valueOf(loc.getY()),
							String.valueOf(loc.getZ()),
							loc.getWorld().getName())));
		
		} else if (args[0].equalsIgnoreCase("addcoins")) {
			
			if (!(args.length >= 3)) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare addcoins [player] [amount]");
				return false;
			}
			
			Player coinsPlayer = Bukkit.getServer().getPlayer(args[1]);
			GamePlayer coinsGP = null;
			
			try {
				coinsGP = Warfare.getInstance().getPlayerManager().getPlayer(coinsPlayer.getName());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
			
			int coins = 0;
			
			try {
				coins = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare addcoins [player] [amount]");
				return false;
			}
			
			coinsGP.setCoins(coinsGP.getCoins() + coins);
			
			coinsGP.getBukkitPlayer().sendMessage(ChatColor.GOLD + String.format("You received %s coins from %s.", 
					String.valueOf(coins),
					((Player) sender).getDisplayName()));
			sender.sendMessage(ChatColor.GOLD + String.format("You gave %s coins to %s.", 
					String.valueOf(coins),
					coinsGP.getBukkitPlayer().getDisplayName()));
			
		} else if (args[0].equalsIgnoreCase("removecoins")) {
			
			if (!(args.length >= 3)) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare removecoins [player] [amount]");
				return false;
			}
			
			Player coinsPlayer = Bukkit.getServer().getPlayer(args[1]);
			GamePlayer coinsGP = null;
			
			try {
				coinsGP = Warfare.getInstance().getPlayerManager().getPlayer(coinsPlayer.getName());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
			
			int coins = 0;
			
			try {
				coins = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare removecoins [player] [amount]");
				return false;
			}
			
			coinsGP.setCoins(coinsGP.getCoins() - coins);
			
			coinsGP.getBukkitPlayer().sendMessage(ChatColor.GOLD + String.format("%s took away %s of your coins.",
					((Player) sender).getDisplayName(),
					String.valueOf(coins)));
			sender.sendMessage(ChatColor.GOLD + String.format("You took %s coins from %s.", 
					String.valueOf(coins),
					coinsGP.getBukkitPlayer().getDisplayName()));
			
		} else if (args[0].equalsIgnoreCase("removecage")) {
			
			if (!sender.hasPermission("warfare.removecage")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare removecage [name]");
				return false;
			}
			
			Arena arena = Warfare.getInstance().getArena();
			
			if (!arena.isEdit()) {
				sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
				sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
				return false;
			}
			
			if (arena.getCageLocations().containsKey(args[1])) {
				sender.sendMessage(ChatColor.RED + "A cage with that name already exists.");
				return false;
			}
			
			arena.getCageLocations().remove(args[1]);
			arena.save();
			
			sender.sendMessage(String.format(ChatColor.GOLD + "You removed the cage %s.", args[1]));

		} else if (args[0].equalsIgnoreCase("addchest")) {
			
			if (!sender.hasPermission("warfare.addchest")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare addchest [tier]");
				return false;
			}
			
			BlockIterator iterator = new BlockIterator(player.getLocation(), player.getEyeHeight());
			Block block = null;
			
			while (iterator.hasNext()) {
				block = iterator.next();
				
				if (!block.getType().equals(Material.AIR))
					break;
			}
			
			if (block == null || block.getType() != Material.CHEST) {
				sender.sendMessage(ChatColor.RED + "The target block is not a chest.");
				return false;
			}
			
			Arena arena = Warfare.getInstance().getArena();
			
			if (arena == null) {
				sender.sendMessage(ChatColor.RED + "The arena is null.");
				return false;
			}
			
			try {
				arena.getLootChests().add(new LootChest(block.getLocation(), Byte.valueOf(args[1])));
				
				sender.sendMessage(ChatColor.GOLD + "Loot chest has been added successfully.");
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Usage: /warfare addchest [tier]");
			}
			
			arena.save();
			
		} else if (args[0].equalsIgnoreCase("edit")) {
			
			if (!sender.hasPermission("warfare.edit")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			Arena arena = Warfare.getInstance().getArena();
			
			if (arena == null)
				return false;
			
			if (arena.isEdit()) {
				arena.setEdit(false);
				
				Warfare.getInstance().setGame(new Game());
				
				sender.sendMessage(ChatColor.GOLD + "You have disabled edit mode for the arena.");
			} else {
				Game game = Warfare.getInstance().getGame();
				
				if (game != null)
					game.end();
				
				arena.setEdit(true);
				sender.sendMessage(ChatColor.GOLD + "You have enabled edit mode for the arena.");
				
				for (GamePlayer gp : game.getPlayers())
					if (gp.isCaged())
						gp.getCage().setPlayer(null);
			}
			
		} else if (args[0].equalsIgnoreCase("start")) {
			
			if (!sender.hasPermission("warfare.start")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			Game game = Warfare.getInstance().getGame();
			
			game.setStage(Game.Stage.BATTLE);
			
		} else if (args[0].equalsIgnoreCase("restart")) {
			
			if (!sender.hasPermission("warfare.restart")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
				return false;
			}
			
			Game game = Warfare.getInstance().getGame();
			
			game.setStage(Game.Stage.RESTART);
		}
		
		return true;
	}
}