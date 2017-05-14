package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.Arena;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddCageArgument extends CommandArgument {

    public AddCageArgument() {

        super("addcage", "Add a cage to the map");

        isPlayerOnly = true;
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("warfare.addcage")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addcage [name]");
            return false;
        }

        Arena arena = Warfare.getInstance().getArena();

        if (!arena.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        if (arena.getCageLocations().containsKey(args[1])) {
            sender.sendMessage(ChatColor.RED + "A cage with that name already exists.");
            return false;
        }

        Location loc = ((Player) sender).getLocation();

        arena.addCageLocation(args[1], loc);
        arena.save();

        sender.sendMessage(String.format(ChatColor.GOLD + "You created the cage %s at %s.",
                args[1],
                String.format("X:%s Y:%s Z:%s world: %s",
                        String.valueOf(loc.getX()),
                        String.valueOf(loc.getY()),
                        String.valueOf(loc.getZ()),
                        loc.getWorld().getName())));

        return true;
    }
}