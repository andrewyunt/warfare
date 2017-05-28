package com.andrewyunt.warfare.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location spawnLocation = player.getLocation().getWorld().getSpawnLocation();
            player.teleport(new Location(spawnLocation.getWorld(), spawnLocation.getX() + 0.5,
                    spawnLocation.getY(), spawnLocation.getZ() + 0.5, 90, 0));
            player.sendMessage(ChatColor.YELLOW + "Teleported to the spawn location.");
        }

        return true;
    }
}