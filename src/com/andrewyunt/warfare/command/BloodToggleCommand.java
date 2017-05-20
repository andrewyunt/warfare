package com.andrewyunt.warfare.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

/**
 * The bloodtoggle command class which is used as a Bukkit CommandExecutor
 * to toggle blood particles on or off.
 *
 * @author Andrew Yunt
 */
public class BloodToggleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            System.out.println("You may not execute that command from the console.");
            return false;
        }

        if (!sender.hasPermission("warfare.bloodtoggle")) {
            sender.sendMessage(ChatColor.YELLOW + "You do not have access to that command.");
            return false;
        }

        GamePlayer player = Warfare.getInstance().getPlayerManager().getPlayer(sender.getName());

        boolean hasBloodEffect = !player.getHasBloodEffect();

        player.setHasBloodEffect(hasBloodEffect);

        player.getBukkitPlayer().sendMessage(ChatColor.YELLOW + String.format(
                "Blood particles toggled %s successfully",
                ChatColor.GOLD + (hasBloodEffect ? "on" : "off") + ChatColor.YELLOW));

        return true;
    }
}