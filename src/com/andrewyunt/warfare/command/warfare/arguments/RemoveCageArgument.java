package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Arena;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RemoveCageArgument extends CommandArgument {

    public RemoveCageArgument() {
        super("removecage", "Remove a cage from the map");

        isPlayerOnly = true;
        permission = "warfare.removecage";
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("warfare.removecage")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare removecage [name]");
            return false;
        }

        Arena arena = Warfare.getInstance().getArena();

        if (!arena.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        if (!arena.getCageLocations().containsKey(args[1])) {
            sender.sendMessage(ChatColor.RED + "A cage with that name does not already exist.");
            return false;
        }

        arena.getCageLocations().remove(args[1]);
        Warfare.getInstance().getStorageManager().saveArena();

        sender.sendMessage(String.format(ChatColor.YELLOW + "You removed the cage " + ChatColor.GOLD + "%s.", args[1]));

        return true;
    }
}