package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.GamePlayer;
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
        GamePlayer coinsGP = null;

        try {
            coinsGP = Warfare.getInstance().getPlayerManager().getPlayer(coinsPlayer.getName());
        } catch (PlayerException e) {
            e.printStackTrace();
        }

        int coins;

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

        return true;
    }
}