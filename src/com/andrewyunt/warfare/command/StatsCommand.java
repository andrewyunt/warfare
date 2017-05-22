package com.andrewyunt.warfare.command;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player targetPlayer;

            if (args.length == 0) {
                targetPlayer = (Player) sender;
            } else {
                targetPlayer = Bukkit.getServer().getPlayer(args[0]);
            }

            GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(targetPlayer);
            String[] messages = new String[] {
                    ChatColor.GOLD + "Kills: " + ChatColor.GRAY + gp.getKills(),
                    ChatColor.GOLD + "Deaths: " + ChatColor.GRAY + gp.getDeaths(),
                    ChatColor.GOLD + "KDR: " + ChatColor.GRAY + gp.getKills() / gp.getDeaths(),
                    ChatColor.GOLD + "Kills: " + ChatColor.GRAY + gp.getKills(),
                    ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "-----------------------------",
                    ChatColor.GOLD + "Wins: " + ChatColor.GRAY + gp.getWins(),
                    ChatColor.GOLD + "Losses: " + ChatColor.GRAY + gp.getLosses(),
                    ChatColor.GOLD + "Games Played: " + ChatColor.GOLD + gp.getGamesPlayed()
            };

            for (String message : messages) {
                sender.sendMessage(message);
            }
        }

        return true;
    }
}