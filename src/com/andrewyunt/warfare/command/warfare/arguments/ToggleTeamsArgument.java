package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ToggleTeamsArgument extends CommandArgument {

    public ToggleTeamsArgument() {
        super("toggleteams", "Toggle whether the map is teams or not");

        isPlayerOnly = true;
        permission = "warfare.toggleteams";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Game game = Warfare.getInstance().getGame();
        Warfare.getInstance().getGame().setTeams(!game.isTeams());

        return true;
    }
}