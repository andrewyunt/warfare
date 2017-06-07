package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWaitingLocationArgument extends CommandArgument {

    public SetWaitingLocationArgument() {
        super("setwaitinglocation", "Sets the waiting location for the map");

        isPlayerOnly = true;
        permission = "warfare.waitinglocation";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Game game = Warfare.getInstance().getGame();

        if (game == null) {
            sender.sendMessage(ChatColor.RED + "The map is null.");
            return false;
        }

        if (!game.isTeams()) {
            sender.sendMessage(ChatColor.RED + "You cannot set the waiting location in a solo game.");
            return false;
        }

        if (!game.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        game.setWaitingLocation(((Player) sender).getLocation());
        Warfare.getInstance().getStorageManager().saveMap();

        sender.sendMessage(ChatColor.YELLOW + "Set the waiting location successfully.");

        return true;
    }
}