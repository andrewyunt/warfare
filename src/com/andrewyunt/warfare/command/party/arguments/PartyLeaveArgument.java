package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyLeaveArgument extends CommandArgument {

    public PartyLeaveArgument() {

        super("leave", "Leave your current party");

        this.isPlayerOnly = true;
        permission = "warfare.party.leave";
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.YELLOW + "You aren't in a party");
            return false;
        }

        if (party.getLeader() == player.getUniqueId()) {
            player.sendMessage(ChatColor.YELLOW + "You must disband your party");
        } else {
            party.getMembers().remove(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You left the party");
        }

        return true;
    }
}