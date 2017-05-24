package com.andrewyunt.warfare.command;

import com.andrewyunt.warfare.Warfare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player targetPlayer;

            if (args.length == 0) {
                targetPlayer = (Player) sender;
            } else {
                targetPlayer = Bukkit.getServer().getPlayer(args[0]);
            }

            int coins = Warfare.getInstance().getPlayerManager().getPlayer(targetPlayer).getCoins();
            sender.sendMessage(ChatColor.GOLD + targetPlayer.getDisplayName() + ChatColor.YELLOW + " has a balance of "
                    + ChatColor.GRAY + coins + " coins");
        }

        return true;
    }
}