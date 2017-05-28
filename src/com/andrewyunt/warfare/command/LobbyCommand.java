package com.andrewyunt.warfare.command;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.concurrent.ThreadLocalRandom;

public class LobbyCommand implements CommandExecutor{

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (StaticConfiguration.LOBBY) {
                Utils.sendPlayerToServer((Player)sender, "Hub" + ThreadLocalRandom.current().nextInt(1, 8 + 1));
            }
            Utils.sendPlayerToServer((Player)sender, StaticConfiguration.getNextLobby());
            sender.sendMessage(ChatColor.YELLOW + "Sending you to the warfare lobby");
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to do this");
        }

        return true;
    }
}
