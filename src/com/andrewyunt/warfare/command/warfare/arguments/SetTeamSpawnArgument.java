package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTeamSpawnArgument extends CommandArgument {

    public SetTeamSpawnArgument() {
        super("setteamspawn", "Set a team's spawn");

        isPlayerOnly = true;
        permission = "warfare.setteamspawn";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Game game = Warfare.getInstance().getGame();

        if (!game.isTeams()) {
            sender.sendMessage(ChatColor.RED + "You cannot set team spawns in a solo game.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare setteamspawn [team]");
            return false;
        }

        if (!game.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        Location loc = ((Player) sender).getLocation();

        try {
            game.getTeamSpawns().put(Integer.valueOf(args[1]), loc);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "You must enter a team number, either 1 or 2.");
            return false;
        }

        Warfare.getInstance().getStorageManager().saveMap();

        sender.sendMessage(String.format(ChatColor.YELLOW + "Set the spawn for team " + ChatColor.GOLD + "%s " + ChatColor.YELLOW + "in" + ChatColor.GOLD + " %s.",
                args[1],
                String.format("X:%s Y:%s Z:%s world: %s",
                        String.valueOf(loc.getBlockX()),
                        String.valueOf(loc.getBlockY()),
                        String.valueOf(loc.getBlockZ()),
                        loc.getWorld().getName())));

        return true;
    }
}