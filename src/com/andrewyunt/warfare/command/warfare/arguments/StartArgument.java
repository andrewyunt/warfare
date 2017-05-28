package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class StartArgument extends CommandArgument{

    public StartArgument() {
        super("start", "Start the game");

        isPlayerOnly = true;
        permission = "warfare.start";
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("warfare.start")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        Game game = Warfare.getInstance().getGame();

        game.setStage(Game.Stage.BATTLE);

        return true;
    }
}
