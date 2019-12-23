package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.utilities.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMapLocationArgument extends CommandArgument {

    public SetMapLocationArgument() {
        super("setmaplocation", "Sets map spawn location");

        isPlayerOnly = true;
        permission = "warfare.setmaplocation";
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

        if (!game.isEdit()) {
            sender.sendMessage(ChatColor.RED + "You must set the map to edit mode before using that command");
            sender.sendMessage(ChatColor.RED + "Usage: /warfare edit");
            return false;
        }

        game.setMapLocation(((Player) sender).getLocation());
        Warfare.getInstance().getStorageManager().saveMap();

        sender.sendMessage(ChatColor.YELLOW + "Set the map location successfully.");

        return true;
    }
}