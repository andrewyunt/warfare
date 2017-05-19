package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.Arena;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMapLocationArgument extends CommandArgument {

    public SetMapLocationArgument() {

        super("setmaplocation", "Sets map spawn location");

        isPlayerOnly = true;
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("warfare.setmaplocation")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        Arena arena = Warfare.getInstance().getArena();

        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "The arena is null.");
            return false;
        }

        arena.setMapLocation(((Player) sender).getLocation());

        arena.save();

        return true;
    }
}