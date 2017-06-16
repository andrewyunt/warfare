package com.andrewyunt.warfare.command;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.framework.BaseConstants;
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
            Player targetPlayer = null;

            if (args.length == 0) {
                targetPlayer = (Player) sender;
            } else {
                try {
                    targetPlayer = Bukkit.getServer().getPlayer(args[0]);
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + "The specified player doesn't exist.");
                }
            }

            GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(targetPlayer);
            String[] messages = new String[] {
                    ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "-----------------------------",
                    ChatColor.GOLD + ChatColor.BOLD.toString() + targetPlayer.getDisplayName() + "'s Stats",
                    ChatColor.YELLOW + " Kills " + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getKills(),
                    ChatColor.YELLOW + " Deaths " + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getDeaths(),
                    ChatColor.YELLOW + " KDR " + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getKills() / gp.getDeaths(),
                    ChatColor.YELLOW + " Kills "  + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getKills(),
                    ChatColor.YELLOW + " Wins "  + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getWins(),
                    ChatColor.YELLOW + " Losses "  + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getLosses(),
                    ChatColor.YELLOW + " Games Played "  + ChatColor.DARK_GRAY + BaseConstants.DOUBLEARROW + " " + ChatColor.GRAY + gp.getGamesPlayed(),
                    ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "-----------------------------"
            };

            for (String message : messages) {
                sender.sendMessage(message);
            }
        }

        return true;
    }
}