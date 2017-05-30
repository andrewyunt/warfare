package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EditArgument extends CommandArgument {

    public EditArgument() {
        super("edit", "Put the server in edit mode");

        isPlayerOnly = true;
        permission = "warfare.edit";
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("warfare.edit")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        Game game = Warfare.getInstance().getGame();

        if (game == null) {
            return false;
        }

        if (game.isEdit()) {
            game.setEdit(false);

            Warfare.getInstance().setGame(new Game());

            sender.sendMessage(ChatColor.YELLOW + "You have disabled edit mode for the arena.");
        } else {
            if (game != null) {
                game.setStage(Game.Stage.END);
            }

            game.setEdit(true);
            sender.sendMessage(ChatColor.YELLOW + "You have enabled edit mode for the arena.");

            for (GamePlayer gp : game.getPlayers()) {
                if (gp.isCaged()) {
                    gp.getCage().setPlayer(null);
                }
            }
        }

        return true;
    }
}