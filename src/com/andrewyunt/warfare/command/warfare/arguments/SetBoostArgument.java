package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetBoostArgument extends CommandArgument {

    public SetBoostArgument() {
        super("setboostargument", "Set a player's booster level");

        isPlayerOnly = true;
        permission = "warfare.setboostargument";
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(args.length >= 3)) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare setboostargument [player] [amount]");
            return false;
        }

        Player targetPlayer = Bukkit.getServer().getPlayer(args[1]);
        GamePlayer targetGP = Warfare.getInstance().getPlayerManager().getPlayer(targetPlayer.getName());

        int boost;

        try {
            boost = Integer.valueOf(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare setboostargument [player] [amount]");
            return false;
        }

        targetGP.setBoost(boost);

        sender.sendMessage(String.format(ChatColor.YELLOW + "You set" + ChatColor.GOLD + "%s" + ChatColor.YELLOW
                        + "'s booster level to " + ChatColor.GOLD + "%s"+ ChatColor.YELLOW + ".",
                targetGP.getBukkitPlayer().getDisplayName(),
                String.valueOf(boost)));

        return true;
    }
}