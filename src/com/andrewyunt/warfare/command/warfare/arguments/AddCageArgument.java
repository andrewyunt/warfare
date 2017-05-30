package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Game;
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
        permission = "warfare.addcage";
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
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addcage [name] [side]");
            return false;
        }

        Game game = Warfare.getInstance().getGame();

        if (!game.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        if (game.getCage(args[1]) != null) {
            sender.sendMessage(ChatColor.RED + "A cage with that name already exists.");
            return false;
        }

        Location loc = ((Player) sender).getLocation();

        game.getCages().add(new Cage(args[1], loc, Integer.valueOf(args[2])));
        Warfare.getInstance().getStorageManager().saveMap();

        sender.sendMessage(String.format(ChatColor.YELLOW + "You created the cage " + ChatColor.GOLD + "%s " + ChatColor.YELLOW + "in" + ChatColor.GOLD + " %s.",
                args[1],
                String.format("X:%s Y:%s Z:%s world: %s",
                        String.valueOf(loc.getBlockX()),
                        String.valueOf(loc.getBlockY()),
                        String.valueOf(loc.getBlockZ()),
                        loc.getWorld().getName())));

        return true;
    }
}