package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Transaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLevelArgument extends CommandArgument {

    public SetLevelArgument() {
        super("setlevel", "Set a player's levels");

        isPlayerOnly = true;
        permission = "warfare.setlevel";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(args.length >= 3)) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare setlevel [player] [amount]");
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> {
            OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
            int level;

            try {
                level = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Usage: /warfare setlevel [player] [amount]");
                return;
            }

            String transactionMessage = String.format(ChatColor.GOLD + "%s" + ChatColor.YELLOW
                            + " set your level to " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + ".",
                    ((Player) sender).getDisplayName(),
                    String.valueOf(level));
            Warfare.getInstance().getStorageManager().savePendingTransaction(new Transaction(targetPlayer.getUniqueId(), transactionMessage, 0, level * 150));

            sender.sendMessage(ChatColor.YELLOW + String.format("You set " + ChatColor.GOLD + "%s's "
                            + ChatColor.YELLOW + "level to " + ChatColor.GOLD + "%s.",
                    targetPlayer.getName(),
                    String.valueOf(level)));
        });

        return true;
    }
}