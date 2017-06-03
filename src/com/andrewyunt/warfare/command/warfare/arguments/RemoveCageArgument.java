package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Game;
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

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare removecage [name]");
            return false;
        }

        Game game = Warfare.getInstance().getGame();

        if (!game.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        Cage cage = game.getCage(args[1]);

        if (cage == null) {
            sender.sendMessage(ChatColor.RED + "A cage with that name does not exist.");
            return false;
        }

        game.getCages().remove(cage);
        Warfare.getInstance().getStorageManager().saveMap();

        sender.sendMessage(String.format(ChatColor.YELLOW + "You removed the cage " + ChatColor.GOLD + "%s.", args[1]));

        return true;
    }
}