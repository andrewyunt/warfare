package com.andrewyunt.warfare.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor{

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.hasPermission("warfare.setspawn")) {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                Location playerLoc = player.getLocation();
                Location spawnLoc = player.getWorld().getSpawnLocation();

                spawnLoc.setX(playerLoc.getBlockX() + 0.5);
                spawnLoc.setY(playerLoc.getY());
                spawnLoc.setZ(playerLoc.getBlockZ() + 0.5);
                spawnLoc.setYaw(playerLoc.getPitch());
                spawnLoc.setPitch(playerLoc.getPitch());

                player.sendMessage(ChatColor.YELLOW + "Spawn location set successfully for world "
                        + ChatColor.GOLD + spawnLoc.getWorld().getName() + ChatColor.YELLOW + ".");
            }
        }

        return true;
    }
}
