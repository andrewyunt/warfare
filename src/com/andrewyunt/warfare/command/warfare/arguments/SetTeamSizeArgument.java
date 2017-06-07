package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTeamSizeArgument extends CommandArgument {

    public SetTeamSizeArgument() {
        super("setteamspawn", "Set a the team size");

        isPlayerOnly = true;
        permission = "warfare.setteamsize";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Game game = Warfare.getInstance().getGame();

        if (!game.isTeams()) {
            sender.sendMessage(ChatColor.RED + "You cannot set team sizes in a solo game.");
            return false;
        }

        if (!game.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        Location loc = ((Player) sender).getLocation();

        try {
            game.setTeamSize(Integer.valueOf(args[1]));
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "You must enter a team size (integer).");
            return false;
        }

        Warfare.getInstance().getStorageManager().saveMap();

        sender.sendMessage(ChatColor.YELLOW + "Set the team size to " + ChatColor.GOLD + args[1] + ChatColor.YELLOW + ".");

        return true;
    }
}