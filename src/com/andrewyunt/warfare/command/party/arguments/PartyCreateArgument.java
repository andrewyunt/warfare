package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.managers.PartyManager;
import com.andrewyunt.warfare.player.Party;
import com.andrewyunt.warfare.utilities.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCreateArgument extends CommandArgument {

    public PartyCreateArgument() {
        super("create", "Create a party");

        this.isPlayerOnly = true;
        permission = "warfare.party.create";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PartyManager partyManager = Warfare.getInstance().getPartyManager();
        Player player = (Player) sender;

        if (partyManager.getParty(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.YELLOW + "You are already in a party");
        } else {
            Party party = partyManager.createParty(player.getUniqueId());
            Warfare.getInstance().getStorageManager().saveParty(party);
            player.sendMessage(ChatColor.YELLOW + "You created a party.");
        }

        return true;
    }
}