package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyDisbandArgument extends CommandArgument {

    public PartyDisbandArgument() {

        super("disband", "Disband your party");

        this.isPlayerOnly = true;
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.YELLOW + "You are not in a party");
            return false;
        }

        if (party.getLeader() == player.getUniqueId()) {
            Warfare.getInstance().getPartyManager().deleteParty(party);
        } else {
            player.sendMessage(ChatColor.YELLOW + "You need to be the leader of a party to disband it.");
        }

        return true;
    }
}