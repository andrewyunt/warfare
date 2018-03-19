package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Transaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCoinsArgument extends CommandArgument {

    public RemoveCoinsArgument() {
        super("removecoins", "Remove coins from a player's balance");

        isPlayerOnly = true;
        permission = "warfare.removecoins";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(args.length >= 3)) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare removecoins [player] [amount]");
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> {
            OfflinePlayer coinsPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
            int coins;

            try {
                coins = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Usage: /warfare removecoins [player] [amount]");
                return;
            }

            String transactionMessage = ChatColor.YELLOW + String.format("%s " + ChatColor.YELLOW + "took away " + ChatColor.GOLD + "%s " + ChatColor.YELLOW + "of your coins.",
                    ((Player) sender).getDisplayName(),
                    String.valueOf(coins));
            Warfare.getInstance().getStorageManager().savePendingTransaction(new Transaction(coinsPlayer.getUniqueId(), transactionMessage, - coins, 0));

            sender.sendMessage(ChatColor.YELLOW + String.format("You took " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + " coins from " + ChatColor.GOLD + "%s.",
                    String.valueOf(coins),
                    coinsPlayer.getName()));
        });

        return true;
    }
}