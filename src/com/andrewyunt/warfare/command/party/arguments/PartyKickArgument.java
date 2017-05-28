package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyKickArgument extends CommandArgument{

    public PartyKickArgument() {

        super("kick", "Kick a player from your party");

        this.isPlayerOnly = true;
        permission = "warfare.party.kick";
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName() + " <player>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + getUsage(label));
            return true;
        }

        Player player = (Player) sender;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.YELLOW + "You need to be in a party to do this");
            return false;
        }

        UUID kick = Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId(); //TODO: Don't use Bukkit.getOfflinePlayer()

        if (kick == null) {
            player.sendMessage(ChatColor.YELLOW + "Player not found");
        } else if (kick == player.getUniqueId()) {
            player.sendMessage(ChatColor.YELLOW + "You may not kick yourself");
        } else if (party.getMembers().contains(kick)) {
            player.sendMessage(ChatColor.YELLOW + "That player is not in your party");
        } else {
            party.getMembers().remove(kick);
        }

        Warfare.getInstance().getStorageManager().saveParty(party);

        return true;
    }
}