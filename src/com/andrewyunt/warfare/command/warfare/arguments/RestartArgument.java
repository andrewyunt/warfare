package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RestartArgument extends CommandArgument {

    public RestartArgument() {
        super("restart", "Restart the server");

        isPlayerOnly = true;
        permission = "warfare.restart";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Game game = Warfare.getInstance().getGame();
        game.setStage(Game.Stage.RESTART);

        return true;
    }
}
