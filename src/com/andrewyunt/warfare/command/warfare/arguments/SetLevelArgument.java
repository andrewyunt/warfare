package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLevelArgument extends CommandArgument {

    public SetLevelArgument() {

        super("setlevel", "Set a player's levels");

        isPlayerOnly = true;
        permission = "warfare.setlevel";
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(args.length >= 3)) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare setlevel [player] [amount]");
            return false;
        }

        Player targetPlayer = Bukkit.getServer().getPlayer(args[1]);
        GamePlayer targetGP = Warfare.getInstance().getPlayerManager().getPlayer(targetPlayer);

        int level;

        try {
            level = Integer.valueOf(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare setlevel [player] [amount]");
            return false;
        }

        targetGP.setPoints(level * 150);

        targetGP.getBukkitPlayer().sendMessage(String.format(ChatColor.GOLD + "%s" + ChatColor.YELLOW
                        + " set your level to " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + ".",
                ((Player) sender).getDisplayName(),
                String.valueOf(level)));
        sender.sendMessage(ChatColor.YELLOW + String.format("You set " + ChatColor.GOLD + "%s's "
                        + ChatColor.YELLOW + "level to " + ChatColor.GOLD + "%s.",
                targetGP.getBukkitPlayer().getDisplayName(),
                String.valueOf(level)));

        return true;
    }
}