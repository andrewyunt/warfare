package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PartyDisbandArgument extends CommandArgument {

    public PartyDisbandArgument() {
        super("disband", "Disband your party");

        this.isPlayerOnly = true;
        permission = "warfare.party.disband";
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

        if (Objects.equals(party.getLeader(), player.getUniqueId())) {
            Warfare.getInstance().getPartyManager().deleteParty(party);
            Warfare.getInstance().getStorageManager().saveParty(party);
            for (UUID uuid : party.getMembers()) {
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.YELLOW + "Your party has been disbanded.");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "You need to be the leader of a party to disband it.");
        }

        return true;
    }
}