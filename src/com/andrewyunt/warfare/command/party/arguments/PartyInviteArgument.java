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

public class PartyInviteArgument extends CommandArgument{

    public PartyInviteArgument() {
        super("invite", "Invite a player to your party");

        this.isPlayerOnly = true;
        permission = "warfare.party.invite";
    }

    public String getUsage(String s)
    {
        return "/" + s + " " + getName() + " <name>";
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.YELLOW + getUsage(label));
            return true;
        }

        Player player = (Player) commandSender;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.YELLOW + "You need to be in a party to do this");
            return false;
        }

        if (party.getLeader() != player.getUniqueId()) {
            player.sendMessage(ChatColor.YELLOW + "You must be the leader of the party to do this");
            return false;
        }
        if (party.isOpen()) {
            player.sendMessage(ChatColor.YELLOW + "The party is open you cannot invite players.");
            return false;
        }

        UUID invited = Bukkit.getOfflinePlayer(args[1]).getUniqueId();

        if (invited == null) {
            player.sendMessage(ChatColor.YELLOW + "Player not found");
        } else if (Warfare.getInstance().getPartyManager().getParty(invited) != null) {
            player.sendMessage(ChatColor.YELLOW + "That player is already in a party");
        } else if (party.getMembers().contains(invited)) {
            player.sendMessage(ChatColor.YELLOW + "That player is already invited");
        } else {
            party.getInvites().add(invited);
            Warfare.getInstance().getStorageManager().saveParty(party);
            Bukkit.getPlayer(invited).sendMessage(ChatColor.YELLOW + "You have been invited to " +
                    ChatColor.GOLD + player.getDisplayName() + ChatColor.YELLOW + "'s party.");
            player.sendMessage(ChatColor.YELLOW + "Invited " + ChatColor.GOLD + args[1] +
                    ChatColor.YELLOW + " to the party.");
        }

        return true;
    }
}