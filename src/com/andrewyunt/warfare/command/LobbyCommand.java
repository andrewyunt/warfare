package com.andrewyunt.warfare.command;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.utilities.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor{
    private final Warfare warfare;

    public LobbyCommand(Warfare warfare) {
        this.warfare = warfare;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Utils.sendPlayerToServer((Player)sender, StaticConfiguration.getNextLobby());
            sender.sendMessage(ChatColor.YELLOW + "Sending you to the warfare lobby");
        }
        else{
            sender.sendMessage(ChatColor.RED + "You must be a player to do this");
        }
        return true;
    }
}
