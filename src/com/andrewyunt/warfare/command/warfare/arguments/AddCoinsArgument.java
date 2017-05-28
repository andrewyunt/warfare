package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddCoinsArgument extends CommandArgument {

    public AddCoinsArgument() {
        super("addcoins", "Add coins to a player's balance");

        isPlayerOnly = true;
        permission = "warfare.addcoins";
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(args.length >= 3)) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addcoins [player] [amount]");
            return false;
        }

        Player coinsPlayer = Bukkit.getServer().getPlayer(args[1]);
        GamePlayer coinsGP = Warfare.getInstance().getPlayerManager().getPlayer(coinsPlayer.getName());

        int coins;

        try {
            coins = Integer.valueOf(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addcoins [player] [amount]");
            return false;
        }

        coinsGP.setCoins(coinsGP.getCoins() + coins);

        coinsGP.getBukkitPlayer().sendMessage(ChatColor.YELLOW + String.format("You received " + ChatColor.GOLD + "%s " + ChatColor.YELLOW + "coins from " + ChatColor.GOLD + "%s.",
                String.valueOf(coins),
                ((Player) sender).getDisplayName()));
        sender.sendMessage(ChatColor.YELLOW + String.format("You gave " + ChatColor.GOLD + "%s " + ChatColor.YELLOW + "coins to " + ChatColor.GOLD + "%s.",
                String.valueOf(coins),
                coinsGP.getBukkitPlayer().getDisplayName()));

        return true;
    }
}