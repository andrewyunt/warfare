package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PartyOpenArgument extends CommandArgument {

    public PartyOpenArgument() {
        super("open", "Open/close your party");

        this.isPlayerOnly = true;
        permission = "warfare.party.open";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.YELLOW + "You are not in a party");
            return false;
        }

        if (Objects.equals(party.getLeader(), player.getUniqueId())) {
            party.setOpen(!party.isOpen());
            Warfare.getInstance().getStorageManager().saveParty(party);
            player.sendMessage(ChatColor.YELLOW + "The party is now " + (party.isOpen() ? ChatColor.GREEN + "open" : ChatColor.RED + "closed"));
        } else {
            player.sendMessage(ChatColor.YELLOW + "You need to be the leader of a party to toggle whether it is open.");
        }

        return true;
    }
}