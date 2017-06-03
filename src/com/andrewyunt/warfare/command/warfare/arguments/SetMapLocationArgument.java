package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
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
            sender.sendMessage(ChatColor.RED + "The arena is null.");
            return false;
        }

        game.setMapLocation(((Player) sender).getLocation());

        Warfare.getInstance().getStorageManager().saveMap();

        return true;
    }
}